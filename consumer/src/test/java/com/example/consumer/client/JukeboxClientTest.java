package com.example.consumer.client;


import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.example.consumer.dto.ActivationRequest;
import com.example.consumer.dto.ActivationResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.*;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

class JukeboxClientTest {

    @Mock
    private WebClient mockWebClient;

    @Mock
    private WebClient.RequestBodyUriSpec mockRequestBodyUriSpec;

    @Mock
    private WebClient.RequestBodySpec mockRequestBodySpec;

    @Mock
    private WebClient.RequestHeadersSpec mockRequestHeadersSpec;

    @Mock
    private WebClient.ResponseSpec mockResponseSpec;

    @InjectMocks
    private JukeboxClient jukeboxClient;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // On crée l’instance réelle du client mais on injecte le mock WebClient
        jukeboxClient = new JukeboxClient("http://localhost:8097");
        TestUtils.setField(jukeboxClient, "webClient", mockWebClient);
    }

    @Test
    void testActivateJukebox_Success() {
        String serialNumber = "SN-12345";
        ActivationResponse mockResponse = ActivationResponse.builder()
                .jukeboxId("jukebox-001").build();

        // Mock du pipeline WebClient
        when(mockWebClient.post()).thenReturn(mockRequestBodyUriSpec);
        when(mockRequestBodyUriSpec.uri("/api/jukebox/activate")).thenReturn(mockRequestBodySpec);
        when(mockRequestBodySpec.contentType(MediaType.APPLICATION_JSON)).thenReturn(mockRequestBodySpec);
        when(mockRequestBodySpec.bodyValue(any(ActivationRequest.class))).thenReturn(mockRequestHeadersSpec);
        when(mockRequestHeadersSpec.retrieve()).thenReturn(mockResponseSpec);
        when(mockResponseSpec.onStatus(any(), any())).thenReturn(mockResponseSpec);
        when(mockResponseSpec.bodyToMono(ActivationResponse.class))
                .thenReturn(Mono.just(mockResponse));

        // Exécution et vérification avec StepVerifier
        StepVerifier.create(jukeboxClient.activateJukebox(serialNumber))
                .expectNextMatches(r -> r.getJukeboxId().equals("jukebox-001"))
                .verifyComplete();

        verify(mockWebClient).post();
        verify(mockRequestBodyUriSpec).uri("/api/jukebox/activate");
        verify(mockResponseSpec).bodyToMono(ActivationResponse.class);
    }

    @Test
    void testActivateJukebox_NotFound() {
        String serialNumber = "SN-404";

        // Mock du pipeline WebClient
        when(mockWebClient.post()).thenReturn(mockRequestBodyUriSpec);
        when(mockRequestBodyUriSpec.uri("/api/jukebox/activate")).thenReturn(mockRequestBodySpec);
        when(mockRequestBodySpec.contentType(MediaType.APPLICATION_JSON)).thenReturn(mockRequestBodySpec);
        when(mockRequestBodySpec.bodyValue(any(ActivationRequest.class))).thenReturn(mockRequestHeadersSpec);
        when(mockRequestHeadersSpec.retrieve()).thenReturn(mockResponseSpec);
        when(mockResponseSpec.onStatus(any(), any())).thenReturn(mockResponseSpec);
        // Simule un 404 comme exception → déclenche onErrorResume
        when(mockResponseSpec.bodyToMono(ActivationResponse.class))
                .thenReturn(Mono.error(new WebClientResponseException(404, "Not Found", null, null, null)));

        // Exécution et vérification
        StepVerifier.create(jukeboxClient.activateJukebox(serialNumber))
                .expectNextMatches(r -> r.getJukeboxId() == null) // fallback
                .verifyComplete();

        verify(mockWebClient).post();
        verify(mockResponseSpec).bodyToMono(ActivationResponse.class);
    }

}
