package com.example.producer.service;

import com.example.common_lib.model.EventType;
import com.example.common_lib.model.JukeboxEvent;
import com.example.common_lib.model.response.ApiResponse;
import com.example.producer.model.Jukeboxe;
import com.example.producer.repo.JukeboxMongoRepo;
import com.example.producer.repo.JukeboxRepository;
import com.example.producer.utils.GlobalVariable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.http.HttpStatus;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

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

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(globalVariable.getPartitionNumber()).thenReturn(3); // exemple
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
}
