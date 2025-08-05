package com.example.producer.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

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
import com.example.producer.service.nosql.PlayRequestService;
import java.time.Instant;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.http.HttpStatus;

class PlayRequestServiceTest {

  @Mock private UserRepository userRepository;
  @Mock private TrackRepository trackRepository;
  @Mock private JukeboxMongoRepo jukeboxRepository;
  @Mock private PlayRequestRepository playRequestRepository;
  @Mock private JukeboxProducerService producerService;

  private final PlayRequestService service =
      new PlayRequestService(
          null, null, null, null, null // Les dépendances ne sont pas nécessaires ici
          );

  @InjectMocks private PlayRequestService playRequestService;

  @BeforeEach
  void setup() {
    MockitoAnnotations.openMocks(this);
  }

  @Test
  void createPlayRequest_shouldThrowNotFoundException_whenUserNotFound() {
    // Arrange
    java.lang.String userId = "user1";
    when(userRepository.existsById(userId)).thenReturn(false);

    // Act & Assert
    NotFoundException ex =
        assertThrows(
            NotFoundException.class,
            () ->
                playRequestService.createPlayRequest(
                    userId,
                    "track1",
                    "jukebox1",
                    EventType.PLAY_REQUEST_EVENT,
                    PriorityLevel.STANDARD));
    assertTrue(ex.getMessage().contains("User ID introuvable"));
    assertEquals("USER_NOT_FOUND", ex.getCode());
  }

  @Test
  void createPlayRequest_shouldReturnApiResponse_whenValidInput() {
    // Arrange
    java.lang.String userId = "user1";
    java.lang.String trackId = "track1";
    java.lang.String jukeboxId = "jukebox1";

    when(userRepository.existsById(userId)).thenReturn(true);
    when(trackRepository.existsById(trackId)).thenReturn(true);
    when(jukeboxRepository.existsById(jukeboxId)).thenReturn(true);

    PlayRequest savedRequest = new PlayRequest();
    savedRequest.setId("pr123");
    savedRequest.setUserId(userId);
    savedRequest.setTrackId(trackId);
    savedRequest.setJukeboxId(jukeboxId);
    savedRequest.setRequestedAt(Instant.now());
    savedRequest.setType(EventType.PLAY_REQUEST_EVENT);
    savedRequest.setStatus("PENDING");
    savedRequest.setPriority(PriorityLevel.STANDARD.name());

    when(playRequestRepository.save(any(PlayRequest.class))).thenReturn(savedRequest);

    // @SuppressWarnings("unchecked")
    ApiResponse<Object> fakeProducerResponse =
        ApiResponse.builder().status(HttpStatus.OK.value()).message("Ok").build();

    when(producerService.sendEventMongo(any(JukeboxEvent.class), anyString()))
        .thenReturn((ApiResponse) fakeProducerResponse);

    // Act
    ApiResponse<JukeboxEvent> response =
        playRequestService.createPlayRequest(
            userId, trackId, jukeboxId, EventType.PLAY_REQUEST_EVENT, PriorityLevel.STANDARD);

    // Assert
    assertEquals(HttpStatus.OK.value(), response.getStatus());
    assertEquals("PLAY_REQUEST_CREATED", response.getCode());
    assertEquals(userId, response.getData().getPayload().get("userId"));
    verify(playRequestRepository).save(any());
    verify(producerService).sendEventMongo(any(JukeboxEvent.class), anyString());
  }

  @Test
  void toJukeboxEvent_shouldConvertCorrectly() {
    // Arrange
    PlayRequest playRequest = new PlayRequest();
    playRequest.setId("pr123");
    playRequest.setUserId("user1");
    playRequest.setTrackId("track1");
    playRequest.setJukeboxId("jukebox1");
    Instant now = Instant.now();
    playRequest.setRequestedAt(now);
    playRequest.setType(EventType.PLAY_REQUEST_EVENT);
    playRequest.setStatus("PENDING");
    playRequest.setPriority(PriorityLevel.STANDARD.name());

    // Act
    JukeboxEvent event = service.toJukeboxEvent(playRequest);

    // Assert
    assertNotNull(event);
    assertEquals("pr123", event.getEventId());
    assertEquals("jukebox1", event.getJukeboxId());
    assertEquals(EventType.PLAY_REQUEST_EVENT, event.getType());
    assertEquals(PriorityLevel.STANDARD, event.getPriority());
    assertEquals(now, event.getTimestamp());

    Map<String, Object> payload = event.getPayload();
    assertNotNull(payload);
    assertEquals("user1", payload.get("userId"));
    assertEquals("track1", payload.get("trackId"));
    assertEquals(now.toString(), payload.get("requestedAt"));
    assertEquals("PENDING", payload.get("status"));
  }
}
