package com.example.consumer.wireMock;


import com.example.consumer.client.JukeboxClient;
import com.example.consumer.dto.ActivationResponse;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;


class JukeboxClientTest {

    @RegisterExtension
    static WireMockExtension wiremock = WireMockExtension.newInstance()
            .options(WireMockConfiguration.wireMockConfig().dynamicPort())
            .build();

    private JukeboxClient jukeboxClient;

    @BeforeEach
    void setup() {
        WebClient webClient = WebClient.builder()
                .baseUrl(wiremock.getRuntimeInfo().getHttpBaseUrl())
                .build();

        jukeboxClient = new JukeboxClient(webClient);
    }

    // ----------------------------------------
    // 1️⃣ TEST : Réponse 200 OK
    // ----------------------------------------
    @Test
    void testActivateJukebox_success() {

        // Mock HTTP
        wiremock.stubFor(post("/api/jukebox/activate")
                .withRequestBody(equalToJson("""
                        {"serialNumber":"SN-123"}
                        """))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                                {"jukeboxId": "JBX-777"}
                                """)));

        Mono<ActivationResponse> result = jukeboxClient.activateJukebox("SN-123");

        StepVerifier.create(result)
                .expectNextMatches(resp -> resp.getJukeboxId().equals("JBX-777"))
                .verifyComplete();
    }

    // ----------------------------------------
    // 2️⃣ TEST : Réponse 404 → Mono.empty()
    // ----------------------------------------
    @Test
    void testActivateJukebox_notFound() {

        wiremock.stubFor(post("/api/jukebox/activate")
                .willReturn(aResponse().withStatus(404)));

        Mono<ActivationResponse> result = jukeboxClient.activateJukebox("SN-404");

        StepVerifier.create(result)
                .expectNextCount(0) // Mono.empty()
                .verifyComplete();
    }

    // ----------------------------------------
    // 3️⃣ TEST : Exception → fallback ActivationResponse(null)
    // ----------------------------------------
    @Test
    void testActivateJukebox_errorFallback() {

        wiremock.stubFor(post("/api/jukebox/activate")
                .willReturn(aResponse().withFixedDelay(5000))); // timeout → exception

        Mono<ActivationResponse> result = jukeboxClient.activateJukebox("SN-TIMEOUT");

        StepVerifier.create(result)
                .expectNextMatches(resp -> resp.getJukeboxId() == null)
                .verifyComplete();
    }
}

