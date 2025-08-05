package com.example.producer.service.nosql;

import com.example.common_lib.model.EventType;
import com.example.common_lib.model.JukeboxEvent;
import com.example.common_lib.model.PriorityLevel;
import com.example.common_lib.model.exception.NotFoundException;
import com.example.common_lib.model.response.ApiResponse;
import com.example.producer.model.PlayRequest;
import com.example.producer.repo.JukeboxMongoRepo;
import com.example.producer.repo.PlayRequestRepository;
import com.example.producer.repo.TrackRepository;
import com.example.producer.repo.UserRepository;
import com.example.producer.service.JukeboxProducerService;
import java.time.Instant;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class PlayRequestService {

  private final UserRepository userRepository;
  private final TrackRepository trackRepository;
  private final JukeboxMongoRepo jukeboxRepository;
  private final PlayRequestRepository playRequestRepository;
  private final JukeboxProducerService producerService;
  private static final String URL_API = "/api/jukebox/events";

  public ApiResponse<JukeboxEvent> createPlayRequest(
      String userId,
      String trackId,
      String jukeboxId,
      EventType string,
      PriorityLevel priority) {

    // Vérifier l'existence des entités référencées
    checkExistsOrThrow(
        userRepository.existsById(userId), "User", userId, "USER_NOT_FOUND", URL_API);
    checkExistsOrThrow(
        trackRepository.existsById(trackId),
        "Track",
        trackId,
        "TRACK_NOT_FOUND",
        URL_API);
    checkExistsOrThrow(
        jukeboxRepository.existsById(jukeboxId),
        "Jukebox",
        jukeboxId,
        "JUKEBOX_NOT_FOUND",
            URL_API);

    // Créer et sauvegarder le PlayRequest
    PlayRequest request = new PlayRequest();
    request.setUserId(userId);
    request.setTrackId(trackId);
    request.setJukeboxId(jukeboxId);
    request.setRequestedAt(Instant.now());
    request.setType(string);
    request.setStatus("PENDING");
    request.setPriority(priority.name());

    PlayRequest savedRequest = playRequestRepository.save(request);

    // Créer un événement à partir du PlayRequest
    JukeboxEvent event = toJukeboxEvent(savedRequest);

    // Envoi de l'événement via le producer
    ApiResponse<?> producerResponse = producerService.sendEventMongo(event, URL_API);
    log.info("🎵 Envoye : {}", event);

    if (producerResponse.getStatus() != HttpStatus.OK.value()) {
      log.warn("L'événement n'a pas été publié correctement : {}", producerResponse.getMessage());
    }

    // Retourner la réponse au client dans le format désiré
    ApiResponse<JukeboxEvent> jukeboxEvent =
        ApiResponse.<JukeboxEvent>builder()
            .timestamp(event.getTimestamp())
            .status(HttpStatus.OK.value())
            .message("Requête de lecture créée et événement publié")
            .code("PLAY_REQUEST_CREATED")
            .eventName(event.getType())
            .data(event)
            .path("/api/play-requests")
            .build();
    log.info("🎵 Envoye : {}", jukeboxEvent);
    return jukeboxEvent;
  }

  public JukeboxEvent toJukeboxEvent(PlayRequest playRequest) {
    Map<java.lang.String, Object> payload = new HashMap<>();
    payload.put("userId", playRequest.getUserId());
    payload.put("trackId", playRequest.getTrackId());
    payload.put("requestedAt", playRequest.getRequestedAt().toString());
    payload.put("status", playRequest.getStatus());

    return JukeboxEvent.builder()
        .eventId(playRequest.getId()) // 🔁 Ici on utilise l'ID du PlayRequest
        .jukeboxId(playRequest.getJukeboxId())
        .type(playRequest.getType())
        .priority(PriorityLevel.valueOf(playRequest.getPriority()))
        .timestamp(playRequest.getRequestedAt())
        .payload(payload)
        .build();
  }

  private void checkExistsOrThrow(
      boolean exists,
      String entityName,
      String entityId,
      String code,
      String path) {
    if (!exists) {
      throw new NotFoundException(entityName + " ID introuvable en base. " + entityId, code, path);
    }
  }

  public Page<PlayRequest> getAllPlayRequests(Pageable pageable, String filter) {
    // Aucun filtre => comportement actuel
    if (filter == null || filter.isEmpty()) {
      return playRequestRepository.findAll(pageable);
    }

    // Exemple filter = "priority:BOOSTED,status:PENDING"
    List<String> filters = Arrays.stream(filter.split(","))
            .map(String::trim)
            .toList();
    return playRequestRepository.findAllWithFilters(pageable, filters);
  }
}
