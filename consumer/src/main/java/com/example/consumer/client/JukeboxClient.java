package com.example.consumer.client;



import com.example.consumer.dto.ActivationRequest;
import com.example.consumer.dto.ActivationResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
@Slf4j
public class JukeboxClient {

    private final WebClient webClient;

    /**
     * Le constructeur reçoit le base URL depuis le fichier application.yml ou application.properties
     */
    public JukeboxClient(@Value("${jukebox.api.url:http://localhost:8097}") String baseUrl) {
        this.webClient = WebClient.builder()
                .baseUrl(baseUrl)
                .build();
    }

    // ✅ Constructeur de test
    public JukeboxClient(WebClient webClient) {
        this.webClient = webClient;
    }

    public JukeboxClient() {
        this.webClient = WebClient.builder()
                .baseUrl("http://localhost:8097")
                .build();
    }

    /**
     * Appel POST pour activer un jukebox via l’API distante
     */
    public Mono<ActivationResponse> activateJukebox(String serialNumber) {
        return webClient.post()
                .uri("/api/jukebox/activate")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new ActivationRequest(serialNumber))
                .retrieve()
                // ⬇️ Intercepte les erreurs HTTP comme 404
                .onStatus(status -> status.value() == 404,
                        response -> Mono.empty())
                .bodyToMono(ActivationResponse.class)
                .onErrorResume(e -> {
                    log.info("❌ Erreur lors de l’activation : {}", e.getMessage());
                    return Mono.just(new ActivationResponse(null)); // fallback neutre
                })
                .doOnNext(response -> log.info("✅ Jukebox activé : {}", response.getJukeboxId()));
    }
}

