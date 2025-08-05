package com.example.producer.service;

import com.example.common_lib.model.EventType;
import com.example.common_lib.model.JukeboxEvent;
import com.example.common_lib.model.response.ApiResponse;
import com.example.producer.model.Jukebox;
import com.example.producer.repo.JukeboxMongoRepo;
import com.example.producer.repo.JukeboxRepository;
import com.example.producer.utils.GlobalVariable;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class JukeboxProducerService {

  private final KafkaTemplate<java.lang.String, JukeboxEvent> kafkaTemplate;
  private final GlobalVariable globalVariable;
  private final JukeboxRepository jukeboxRepository;
  private final JukeboxMongoRepo jukeboxMongoRepo;

  private static final java.lang.String TOPIC_NAME = "jukebox-events";

  public Mono<Object> sendEvent(JukeboxEvent event, java.lang.String path) {
    final java.lang.String jukeboxId = event.getJukeboxId();

    return jukeboxRepository
        .findByJukeboxId(jukeboxId)
        .flatMap(
            jukebox -> {
              prepareEvent(event);
              log.info(
                  "Après prepareEvent: eventId={}, timestamp={}, type={}",
                  event.getEventId(),
                  event.getTimestamp(),
                  event.getType());
              int partition = partitionForJukebox(jukeboxId);
              log.info(
                  "📤 Sending JukeboxEvent to Kafka: eventId={}, jukeboxId={}, partition={}",
                  event.getEventId(),
                  jukeboxId,
                  partition);

              return Mono.create(
                  sink ->
                      kafkaTemplate
                          .send(TOPIC_NAME, partition, jukeboxId, event)
                          .whenComplete(
                              (SendResult<java.lang.String, JukeboxEvent> result, Throwable ex) -> {
                                if (ex == null) {
                                  java.lang.String formattedTime =
                                      DateTimeFormatter.ISO_LOCAL_DATE_TIME
                                          .withZone(ZoneId.systemDefault())
                                          .format(
                                              Instant.ofEpochMilli(
                                                  result.getRecordMetadata().timestamp()));

                                  log.info(
                                      "✅ Event sent: eventId={} | time={} | partition={} |"
                                          + " jukeboxId={}",
                                      event.getEventId(),
                                      formattedTime,
                                      result.getRecordMetadata().partition(),
                                      jukeboxId);

                                  // Retourner un map dans data
                                  Map<java.lang.String, java.lang.String> dataMap =
                                      new ConcurrentHashMap<>();
                                  dataMap.put("eventId", event.getEventId());

                                  sink.success(
                                      ApiResponse.<Map>builder()
                                          .timestamp(Instant.now())
                                          .status(HttpStatus.OK.value())
                                          .message("Événement envoyé avec succès.")
                                          // .code("EVENT_SENT")
                                          .eventName(EventType.PLAY_REQUEST_EVENT)
                                          .data(dataMap)
                                          .path(path)
                                          .build());
                                } else {
                                  log.error(
                                      "❌ Kafka send error: eventId={} | jukeboxId={} | error={}",
                                      event.getEventId(),
                                      jukeboxId,
                                      ex.getMessage());

                                  sink.success(
                                      ApiResponse.<java.lang.String>builder()
                                          .timestamp(Instant.now())
                                          .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                                          .message(
                                              "Erreur lors de l'envoi Kafka : " + ex.getMessage())
                                          .code("KAFKA_SEND_FAILED")
                                          .path(path)
                                          .build());
                                }
                                log.info("🎵 Reçu JukeboxEvent: {}", event);
                              }));
            })
        .switchIfEmpty(
            Mono.just(
                ApiResponse.<java.lang.String>builder()
                    .timestamp(Instant.now())
                    .status(HttpStatus.NOT_FOUND.value())
                    .message("Jukebox ID introuvable en base.")
                    .code("JUKBOX_NOT_FOUND")
                    .path(path)
                    .build()));
  }

  private void prepareEvent(JukeboxEvent event) {
    if (event.getEventId() == null || event.getEventId().isBlank()) {
      event.setEventId(UUID.randomUUID().toString());
    }
    if (event.getTimestamp() == null) {
      event.setTimestamp(Instant.now());
    }
    if (event.getType() == null) {
      event.setType(EventType.PLAY_REQUEST_EVENT);
    }
  }

  private int partitionForJukebox(java.lang.String jukeboxId) {
    return Math.floorMod(jukeboxId.hashCode(), globalVariable.getPartitionNumber());
  }

  public ApiResponse<?> sendEventMongo(JukeboxEvent event, java.lang.String path) {
    final java.lang.String jukeboxId = event.getJukeboxId();

    Optional<Jukebox> jukebox = jukeboxMongoRepo.findById(jukeboxId);
    if (jukebox == null) {
      return ApiResponse.builder()
          .timestamp(Instant.now())
          .status(HttpStatus.NOT_FOUND.value())
          .message("Jukebox ID introuvable en base.")
          .code("JUKEBOX_NOT_FOUND")
          .path(path)
          .build();
    }

    prepareEvent(event);

    int partition = partitionForJukebox(jukeboxId);
    log.info(
        "📤 Sending JukeboxEvent to Kafka: eventId={}, jukeboxId={}, partition={}",
        event.getEventId(),
        jukeboxId,
        partition);

    try {
      SendResult<java.lang.String, JukeboxEvent> result =
          kafkaTemplate.send(TOPIC_NAME, partition, jukeboxId, event).get();

      java.lang.String formattedTime =
          DateTimeFormatter.ISO_LOCAL_DATE_TIME
              .withZone(ZoneId.systemDefault())
              .format(Instant.ofEpochMilli(result.getRecordMetadata().timestamp()));

      log.info(
          "✅ Event sent: eventId={} | time={} | partition={} | jukeboxId={}",
          event.getEventId(),
          formattedTime,
          result.getRecordMetadata().partition(),
          jukeboxId);

      Map<java.lang.String, java.lang.String> dataMap = new HashMap<>();
      dataMap.put("eventId", event.getEventId());

      return ApiResponse.builder()
          .timestamp(Instant.now())
          .status(HttpStatus.OK.value())
          .message("Événement envoyé avec succès.")
          .eventName(EventType.PLAY_REQUEST_EVENT)
          .data(dataMap)
          .path(path)
          .build();

    } catch (Exception ex) {
      log.error(
          "❌ Kafka send error: eventId={} | jukeboxId={} | error={}",
          event.getEventId(),
          jukeboxId,
          ex.getMessage());

      return ApiResponse.builder()
          .timestamp(Instant.now())
          .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
          .message("Erreur lors de l'envoi Kafka : " + ex.getMessage())
          .code("KAFKA_SEND_FAILED")
          .path(path)
          .build();
    }
  }
}
