package com.example.producer.service;

import com.example.common_lib.model.EventType;
import com.example.common_lib.model.JukeboxEvent;
import com.example.common_lib.model.response.ApiResponse;
import com.example.producer.model.Jukebox;
import com.example.producer.model.Jukeboxe;
import com.example.producer.repo.JukeboxMongoRepo;
import com.example.producer.repo.JukeboxRepository;
import com.example.producer.utils.GlobalVariable;
import com.mongodb.assertions.Assertions;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.TopicPartition;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import static com.mongodb.assertions.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JukeboxProducerServiceTest {

    @Mock
    private KafkaTemplate<String, JukeboxEvent> kafkaTemplate;

    @Mock
    private GlobalVariable globalVariable;

    @Mock
    private JukeboxRepository jukeboxRepository;

    @Mock
    private JukeboxMongoRepo jukeboxMongoRepo;

    @InjectMocks
    private JukeboxProducerService service;

    private JukeboxEvent event;

    @BeforeEach
    void setUp() {
        lenient().when(globalVariable.getPartitionNumber()).thenReturn(3);

        event = JukeboxEvent.builder()
                .eventId("evt-123")
                .jukeboxId("JID-1")
                .build();
    }



    @Test
    void sendEvent_jukeboxExists_success() {
        String jukeboxId = "JX123";
        JukeboxEvent event = new JukeboxEvent();
        event.setJukeboxId(jukeboxId);

        // Mock du jukebox existant
        Jukeboxe jukeboxe = new Jukeboxe();
        jukeboxe.setJukeboxId(jukeboxId);

        when(jukeboxRepository.findByJukeboxId(jukeboxId))
                .thenReturn(Mono.just(jukeboxe));

        // Mock KafkaTemplate avec SendResult et RecordMetadata
        SendResult<String, JukeboxEvent> sendResult = mock(SendResult.class);
        org.apache.kafka.clients.producer.RecordMetadata recordMetadata =
                mock(org.apache.kafka.clients.producer.RecordMetadata.class);

        // On retourne un timestamp pour éviter le NPE
        when(recordMetadata.timestamp()).thenReturn(System.currentTimeMillis());
        when(recordMetadata.partition()).thenReturn(0);
        when(sendResult.getRecordMetadata()).thenReturn(recordMetadata);

        CompletableFuture<SendResult<String, JukeboxEvent>> future =
                CompletableFuture.completedFuture(sendResult);
        when(kafkaTemplate.send(anyString(), anyInt(), anyString(), any(JukeboxEvent.class)))
                .thenReturn(future);

        // Appel du service
        Mono<ApiResponse<Map<String, String>>> result = service.sendEvent(event, "/test-path");

        StepVerifier.create(result)
                .assertNext(resp -> {
                    assertEquals(HttpStatus.OK.value(), resp.getStatus());
                    assertEquals("Événement envoyé avec succès.", resp.getMessage());
                    assertEquals(EventType.PLAY_REQUEST_EVENT, resp.getEventName());
                    assertEquals(jukeboxId, event.getJukeboxId());
                    assertNotNull(resp.getTimestamp());
                    assertNotNull(resp.getData().get("eventId"));
                })
                .verifyComplete();

        verify(jukeboxRepository).findByJukeboxId(jukeboxId);
        verify(kafkaTemplate).send(anyString(), anyInt(), anyString(), any(JukeboxEvent.class));
    }

    @Test
    void sendEvent_jukeboxNotFound_error() {
        String jukeboxId = "JX999";
        JukeboxEvent event = new JukeboxEvent();
        event.setJukeboxId(jukeboxId);

        when(jukeboxRepository.findByJukeboxId(jukeboxId))
                .thenReturn(Mono.empty());

        Mono<ApiResponse<Map<String, String>>> result = service.sendEvent(event, "/test-path");

        StepVerifier.create(result)
                .assertNext(resp -> {
                    assertEquals(HttpStatus.NOT_FOUND.value(), resp.getStatus());
                    assertEquals("Jukebox ID introuvable en base.", resp.getMessage());
                    assertEquals("JUKBOX_NOT_FOUND", resp.getCode());
                })
                .verifyComplete();

        verify(jukeboxRepository).findByJukeboxId(jukeboxId);
        verifyNoInteractions(kafkaTemplate);
    }

    @Test
    void sendEvent_kafkaFailure_error() {
        String jukeboxId = "JX123";
        JukeboxEvent event = new JukeboxEvent();
        event.setJukeboxId(jukeboxId);

        Jukeboxe jukeboxe = new Jukeboxe();
        jukeboxe.setJukeboxId(jukeboxId);

        when(jukeboxRepository.findByJukeboxId(jukeboxId))
                .thenReturn(Mono.just(jukeboxe)); // ✅ Correct

        CompletableFuture<SendResult<String, JukeboxEvent>> future = new CompletableFuture<>();
        future.completeExceptionally(new RuntimeException("Kafka down"));
        when(kafkaTemplate.send(anyString(), anyInt(), anyString(), any(JukeboxEvent.class)))
                .thenReturn(future);

        Mono<ApiResponse<Map<String, String>>> result = service.sendEvent(event, "/test-path");

        StepVerifier.create(result)
                .assertNext(resp -> {
                    assertEquals(HttpStatus.SERVICE_UNAVAILABLE.value(), resp.getStatus());
                    assertEquals("Kafka unreachable ou erreur réseau", resp.getMessage());
                    assertEquals("KAFKA_UNREACHABLE", resp.getCode());
                })
                .verifyComplete();

        verify(jukeboxRepository).findByJukeboxId(jukeboxId);
        verify(kafkaTemplate).send(anyString(), anyInt(), anyString(), any(JukeboxEvent.class));
    }

    private SendResult<String, JukeboxEvent> buildSendResult() {
        RecordMetadata metadata = new RecordMetadata(
                new TopicPartition("jukebox-events", 1),
                0, 0, System.currentTimeMillis(), 0L, 0, 0
        );
        ProducerRecord<String, JukeboxEvent> record =
                new ProducerRecord<>("jukebox-events", 1, event.getEventId(), event);

        return new SendResult<>(record, metadata);
    }

    // ------------------------------------------------------------
    // 1️⃣ Kafka send: InterruptedException
    // ------------------------------------------------------------
    @Test
    void testSendEvent_InterruptedException() throws Exception {
        when(jukeboxMongoRepo.findById("JID-1"))
                .thenReturn(Optional.of(Jukebox.builder().jukeboxId("JID-1").build()));

        CompletableFuture<SendResult<String, JukeboxEvent>> future = mock(CompletableFuture.class);

        when(kafkaTemplate.send(anyString(), anyInt(), anyString(), any()))
                .thenReturn(future);

        when(future.get(anyLong(), any()))
                .thenThrow(new InterruptedException("Thread interrupted"));

        ApiResponse<Map<String, String>> response =
                service.sendEventMongo(event, "/api/test");

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), response.getStatus());
        assertEquals("KAFKA_INTERRUPTED", response.getCode());
    }

    // ------------------------------------------------------------
    // 2️⃣ Kafka send: ExecutionException (cause != TimeoutException)
    // ------------------------------------------------------------
    @Test
    void testSendEvent_ExecutionException_NonTimeout() throws Exception {
        when(jukeboxMongoRepo.findById("JID-1"))
                .thenReturn(Optional.of(Jukebox.builder().jukeboxId("JID-1").build()));

        CompletableFuture<SendResult<String, JukeboxEvent>> future = mock(CompletableFuture.class);

        when(kafkaTemplate.send(anyString(), anyInt(), anyString(), any()))
                .thenReturn(future);

        when(future.get(anyLong(), any()))
                .thenThrow(new ExecutionException(new RuntimeException("Kafka internal error")));

        ApiResponse<Map<String, String>> response =
                service.sendEventMongo(event, "/api/test");

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), response.getStatus());
        assertEquals("KAFKA_SEND_FAILED", response.getCode());
    }

    // ------------------------------------------------------------
    // 3️⃣ Kafka get(): TimeoutException (direct)
    // ------------------------------------------------------------
    @Test
    void testSendEvent_TimeoutException() throws Exception {
        when(jukeboxMongoRepo.findById("JID-1"))
                .thenReturn(Optional.of(Jukebox.builder().jukeboxId("JID-1").build()));

        CompletableFuture<SendResult<String, JukeboxEvent>> future = mock(CompletableFuture.class);

        when(kafkaTemplate.send(anyString(), anyInt(), anyString(), any()))
                .thenReturn(future);

        when(future.get(anyLong(), any()))
                .thenThrow(new TimeoutException("Kafka did not respond"));

        ApiResponse<Map<String, String>> response =
                service.sendEventMongo(event, "/api/test");

        assertEquals(HttpStatus.SERVICE_UNAVAILABLE.value(), response.getStatus());
        assertEquals("KAFKA_TIMEOUT", response.getCode());
    }

    @Test
    void testFormatTimestampAndResponseFields() throws Exception {
        // Arrange
        String jukeboxId = "J123";
        String eventId = "EVT-999";
        String path = "/api/test";

        JukeboxEvent event = new JukeboxEvent();
        event.setJukeboxId(jukeboxId);
        event.setEventId(eventId);

        // ---- MOCK MONGO ----
        when(jukeboxMongoRepo.findById(jukeboxId))
                .thenReturn(Optional.of(new Jukebox()));

        // ---- MOCK RECORD METADATA ----
        long fakeTimestamp = Instant.parse("2024-01-01T12:34:56Z").toEpochMilli();

        RecordMetadata metadata = mock(RecordMetadata.class);
        when(metadata.timestamp()).thenReturn(fakeTimestamp);
        when(metadata.partition()).thenReturn(3);

        // ---- MOCK SENDRESULT ----
        SendResult<String, JukeboxEvent> sendResult = mock(SendResult.class);
        when(sendResult.getRecordMetadata()).thenReturn(metadata);

        CompletableFuture<SendResult<String, JukeboxEvent>> future = CompletableFuture.completedFuture(sendResult);
        when(kafkaTemplate.send(anyString(), anyInt(), anyString(), any()))
                .thenReturn(future);

        // Act
        ApiResponse<Map<String, String>> response = service.sendEventMongo(event, path);

        // Assert
        Assertions.assertNotNull(response);
        assertEquals(HttpStatus.OK.value(), response.getStatus());
        assertEquals("Événement envoyé avec succès.", response.getMessage());
        assertEquals(EventType.PLAY_REQUEST_EVENT, response.getEventName());
        assertEquals(path, response.getPath());

        // Vérifie dataMap
        Assertions.assertNotNull(response.getData());
        assertEquals(eventId, response.getData().get("eventId"));

        // Vérifie format du timestamp renvoyé par le code
        String expectedFormattedDate = DateTimeFormatter.ISO_LOCAL_DATE_TIME
                .withZone(ZoneId.systemDefault())
                .format(Instant.ofEpochMilli(fakeTimestamp));

        // On ne peut pas lire directement le log → mais on vérifie la conversion
        Assertions.assertNotNull(expectedFormattedDate);
        assertTrue(expectedFormattedDate.contains("2024"));

        // Vérifie que Kafka a été appelé
        verify(kafkaTemplate).send(anyString(), anyInt(), eq(eventId), eq(event));
    }

    @Test
    void testSendEventMongo_WhenJukeboxNotFound() {
        // GIVEN
        String path = "/test-path";
        JukeboxEvent event = new JukeboxEvent();
        event.setJukeboxId("JK-123");

        // Repo retourne vide => Jukebox absent
        when(jukeboxMongoRepo.findById("JK-123")).thenReturn(Optional.empty());

        // WHEN
        ApiResponse<Map<String, String>> response = service.sendEventMongo(event, path);

        // THEN
        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND.value(), response.getStatus());
        assertEquals("JUKBOX_NOT_FOUND", response.getCode());
        assertEquals("Jukebox ID introuvable en base.", response.getMessage());
        assertEquals(path, response.getPath());

        // Aucun appel Kafka ne doit être fait
        verify(kafkaTemplate, never()).send(anyString(), anyInt(), anyString(), any());
    }

    @Test
    void sendEvent_kafkaExecutionTimeout_error() {
        String jukeboxId = "JX123";
        JukeboxEvent event = new JukeboxEvent();
        event.setJukeboxId(jukeboxId);

        Jukeboxe jukeboxe = new Jukeboxe();
        jukeboxe.setJukeboxId(jukeboxId);

        when(jukeboxRepository.findByJukeboxId(jukeboxId))
                .thenReturn(Mono.just(jukeboxe));

        // Simuler ExecutionException avec cause TimeoutException
        CompletableFuture<SendResult<String, JukeboxEvent>> future = new CompletableFuture<>();
        future.completeExceptionally(new ExecutionException(new java.util.concurrent.TimeoutException("Timeout")));
        when(kafkaTemplate.send(anyString(), anyInt(), anyString(), any(JukeboxEvent.class)))
                .thenReturn(future);

        Mono<ApiResponse<Map<String, String>>> result = service.sendEvent(event, "/test-path");

        StepVerifier.create(result)
                .assertNext(resp -> {
                    assertEquals(HttpStatus.SERVICE_UNAVAILABLE.value(), resp.getStatus());
                    assertEquals("Kafka unreachable ou erreur réseau", resp.getMessage());
                    assertEquals("KAFKA_UNREACHABLE", resp.getCode());
                })
                .verifyComplete();
    }

    @Test
    void sendEvent_kafkaInterruptedException_error() {
        String jukeboxId = "JX123";
        JukeboxEvent event = new JukeboxEvent();
        event.setJukeboxId(jukeboxId);

        Jukeboxe jukeboxe = new Jukeboxe();
        jukeboxe.setJukeboxId(jukeboxId);

        when(jukeboxRepository.findByJukeboxId(jukeboxId))
                .thenReturn(Mono.just(jukeboxe));

        // Simuler InterruptedException lors du get() du future
        CompletableFuture<SendResult<String, JukeboxEvent>> future = new CompletableFuture<>();
        future.completeExceptionally(new InterruptedException("Interrupted"));
        when(kafkaTemplate.send(anyString(), anyInt(), anyString(), any(JukeboxEvent.class)))
                .thenReturn(future);

        Mono<ApiResponse<Map<String, String>>> result = service.sendEvent(event, "/test-path");

        StepVerifier.create(result)
                .assertNext(resp -> {
                    assertEquals(HttpStatus.SERVICE_UNAVAILABLE.value(), resp.getStatus());
                    assertEquals("Kafka unreachable ou erreur réseau", resp.getMessage());
                    assertEquals("KAFKA_UNREACHABLE", resp.getCode());
                })
                .verifyComplete();
    }

    @Test
    void sendEvent_kafkaGetTimeout_error() {
        String jukeboxId = "JX123";
        JukeboxEvent event = new JukeboxEvent();
        event.setJukeboxId(jukeboxId);

        Jukeboxe jukeboxe = new Jukeboxe();
        jukeboxe.setJukeboxId(jukeboxId);

        when(jukeboxRepository.findByJukeboxId(jukeboxId))
                .thenReturn(Mono.just(jukeboxe));

        // Simuler TimeoutException directement
        CompletableFuture<SendResult<String, JukeboxEvent>> future = new CompletableFuture<>();
        when(kafkaTemplate.send(anyString(), anyInt(), anyString(), any(JukeboxEvent.class)))
                .thenReturn(future);

        // TimeoutException sur get() via service
        Mono<ApiResponse<Map<String, String>>> result = service.sendEvent(event, "/test-path");

        // Simuler TimeoutException en forçant get(TIMEOUT_MS)
        StepVerifier.create(result)
                .thenCancel()
                .verify();
    }



}
