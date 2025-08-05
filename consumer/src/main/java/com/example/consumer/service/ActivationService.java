package com.example.consumer.service;

import com.example.consumer.config.JukeboxConfig;
import com.example.consumer.dto.ActivationRequest;
import com.example.consumer.dto.ActivationResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
@Slf4j
public class ActivationService {

    private static final String CONFIG_FILE = "classpath:jukebox.yml";


    private final WebClient webClient;
    private final JukeboxConfig config;

    public ActivationService(JukeboxConfig config) {
        this.config = config;
        this.webClient = WebClient.builder()
                .baseUrl("http://localhost:8097") // Adresse du backend
                .build();
    }

    public Mono<Void> activate() {
        if (config.getJukeboxId() != null) {
            log.info("Jukebox déjà activé avec ID : {}" , config.getJukeboxId());
            return Mono.empty();
        }

        String serialNumber = config.getSerialNumber();
        if (serialNumber == null || serialNumber.isEmpty()) {
            log.info("Serial number non défini dans la configuration");
            return Mono.error(new IllegalStateException("Serial number manquant"));
        }

        return webClient.post()
                .uri("/api/jukebox/activate")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new ActivationRequest(serialNumber))
                .retrieve()
                .bodyToMono(ActivationResponse.class)
                .doOnNext(response -> {
                    config.setJukeboxId(response.getJukeboxId());
                    config.save(CONFIG_FILE);
                    log.info("Jukebox activé avec ID : {} " , response.getJukeboxId());
                })
                .then();
    }
}
