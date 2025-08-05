package com.example.producer.service;

import com.example.common_lib.model.EventType;
import com.example.common_lib.model.JukeboxEvent;
import com.example.common_lib.model.response.ApiResponse;
import com.example.producer.model.Jukebox;
import com.example.producer.repo.JukeboxMongoRepo;
import com.example.producer.repo.JukeboxRepository;
import com.example.producer.utils.GlobalVariable;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Slf4j
@Service
@RequiredArgsConstructor
public class JukeboxProducerService {

  private final KafkaTemplate<String, JukeboxEvent> kafkaTemplate;
  private final GlobalVariable globalVariable;
  private final JukeboxRepository jukeboxRepository;
  private final JukeboxMongoRepo jukeboxMongoRepo;

  private static final String TOPIC_NAME = "jukebox-events";
  private static final long TIMEOUT_MS = 5000L; // 5 secondes

  /** Version reactive */
  public Mono<ApiResponse<Map<String, String>>> sendEvent(JukeboxEvent event, String path) {
    final String jukeboxId = event.getJukeboxId();

    return jukeboxRepository.findByJukeboxId(jukeboxId)
            .flatMap(jukebox -> {
              prepareEvent(event);
              int partition = partitionForJukebox(jukeboxId);
              log.info("📤 Sending JukeboxEvent to Kafka: eventId={}, jukeboxId={}, partition={}",
                      event.getEventId(), jukeboxId, partition);

              CompletableFuture<SendResult<String, JukeboxEvent>> future = kafkaTemplate.send(TOPIC_NAME, partition, jukeboxId, event);

              CompletableFuture<SendResult<String, JukeboxEvent>> safeFuture = future
                      .exceptionally(ex -> {
                        log.error("❌ Kafka send failed immediately: {}", ex.getMessage());
                        return null;
                      });

              return Mono.fromFuture(safeFuture.thenApply(result -> {
                if (result == null) {
                  return buildErrorResponse("KAFKA_UNREACHABLE", "Kafka unreachable ou erreur réseau", path, HttpStatus.SERVICE_UNAVAILABLE);
                }

                String formattedTime = DateTimeFormatter.ISO_LOCAL_DATE_TIME
                        .withZone(ZoneId.systemDefault())
                        .format(Instant.ofEpochMilli(result.getRecordMetadata().timestamp()));

                log.info("✅ Event sent: eventId={} | time={} | partition={} | jukeboxId={}",
                        event.getEventId(), formattedTime,
                        result.getRecordMetadata().partition(), jukeboxId);

                Map<String, String> dataMap = new HashMap<>();
                dataMap.put("eventId", event.getEventId());

                return ApiResponse.<Map<String, String>>builder()
                        .timestamp(Instant.now())
                        .status(HttpStatus.OK.value())
                        .message("Événement envoyé avec succès.")
                        .eventName(EventType.PLAY_REQUEST_EVENT)
                        .data(dataMap)
                        .path(path)
                        .build();
              }));
            })
            .switchIfEmpty(Mono.just(buildErrorResponse("JUKBOX_NOT_FOUND", "Jukebox ID introuvable en base.", path, HttpStatus.NOT_FOUND)));
  }

  /** Version synchrone (Mongo) avec gestion correcte du Timeout et Kafka OFF */
  public ApiResponse<Map<String, String>> sendEventMongo(JukeboxEvent event, String path) {
    final String jukeboxId = event.getJukeboxId();

    Optional<Jukebox> jukebox = jukeboxMongoRepo.findById(jukeboxId);
    if (jukebox.isEmpty()) {
      return buildErrorResponse("JUKBOX_NOT_FOUND", "Jukebox ID introuvable en base.", path, HttpStatus.NOT_FOUND);
    }

    prepareEvent(event);
    int partition = partitionForJukebox(jukeboxId);
    log.info("📤 Sending JukeboxEvent to Kafka: eventId={}, jukeboxId={}, partition={}",
            event.getEventId(), jukeboxId, partition);

    try {
      CompletableFuture<SendResult<String, JukeboxEvent>> future = kafkaTemplate.send(TOPIC_NAME, partition, jukeboxId, event);
      SendResult<String, JukeboxEvent> result = future.get(TIMEOUT_MS, TimeUnit.MILLISECONDS);

      String formattedTime = DateTimeFormatter.ISO_LOCAL_DATE_TIME
              .withZone(ZoneId.systemDefault())
              .format(Instant.ofEpochMilli(result.getRecordMetadata().timestamp()));

      log.info("✅ Event sent: eventId={} | time={} | partition={} | jukeboxId={}",
              event.getEventId(), formattedTime,
              result.getRecordMetadata().partition(), jukeboxId);

      Map<String, String> dataMap = new HashMap<>();
      dataMap.put("eventId", event.getEventId());

      return ApiResponse.<Map<String, String>>builder()
              .timestamp(Instant.now())
              .status(HttpStatus.OK.value())
              .message("Événement envoyé avec succès.")
              .eventName(EventType.PLAY_REQUEST_EVENT)
              .data(dataMap)
              .path(path)
              .build();

    } catch (InterruptedException ex) {
      Thread.currentThread().interrupt();
      log.error("❌ Kafka send interrupted: eventId={} | jukeboxId={}", event.getEventId(), jukeboxId);
      return buildErrorResponse("KAFKA_INTERRUPTED", ex.getMessage(), path, HttpStatus.INTERNAL_SERVER_ERROR);
    } catch (ExecutionException ex) {
      Throwable cause = ex.getCause();
      if (cause != null && cause.getClass().getSimpleName().contains("TimeoutException")) {
        log.error("⏳ Kafka timeout detected: eventId={} | jukeboxId={}", event.getEventId(), jukeboxId);
        return buildErrorResponse("KAFKA_TIMEOUT", "Kafka n'a pas répondu dans le délai de 5 secondes.", path, HttpStatus.SERVICE_UNAVAILABLE);
      }
      log.error("❌ Kafka send execution error: eventId={} | jukeboxId={} | cause={}", event.getEventId(), jukeboxId, cause != null ? cause.getMessage() : ex.getMessage());
      return buildErrorResponse("KAFKA_SEND_FAILED", cause != null ? cause.getMessage() : ex.getMessage(), path, HttpStatus.INTERNAL_SERVER_ERROR);
    } catch (TimeoutException ex) {
      log.error("⏳ Kafka get() timeout: eventId={} | jukeboxId={}", event.getEventId(), jukeboxId);
      return buildErrorResponse("KAFKA_TIMEOUT", "Kafka n'a pas répondu dans le délai de 5 secondes.", path, HttpStatus.SERVICE_UNAVAILABLE);
    }
  }

  /** Prépare l’événement avant envoi */
  private void prepareEvent(JukeboxEvent event) {
    if (event.getEventId() == null || event.getEventId().isBlank()) event.setEventId(UUID.randomUUID().toString());
    if (event.getTimestamp() == null) event.setTimestamp(Instant.now());
    if (event.getType() == null) event.setType(EventType.PLAY_REQUEST_EVENT);
  }

  /** Partition Kafka pour un jukebox */
  private int partitionForJukebox(String jukeboxId) {
    return Math.floorMod(jukeboxId.hashCode(), globalVariable.getPartitionNumber());
  }

  /** Méthode utilitaire pour construire des réponses d’erreur */
  private ApiResponse<Map<String, String>> buildErrorResponse(String code, String message, String path, HttpStatus status) {
    return ApiResponse.<Map<String, String>>builder()
            .timestamp(Instant.now())
            .status(status.value())
            .message(message)
            .code(code)
            .data(Map.of())
            .path(path)
            .build();
  }
}
