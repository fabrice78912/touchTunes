package com.example.producer.service;

import com.example.common_lib.model.response.ApiResponse;
import com.example.producer.model.Jukeboxe;
import com.example.producer.repo.JukeboxRepository;
import java.security.SecureRandom;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@Slf4j
public class JukeboxService {

  private final JukeboxRepository repository;

  private static final String ID_PREFIX = "JX";
  private static final int RANDOM_ID_LENGTH = 7; // Ex: JX9A3B7F
  private static final String ALPHANUM = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
  private final SecureRandom random = new SecureRandom();

  public Mono<ApiResponse<String>> activateJukebox(String serialNumber) {
    return repository
        .findBySerialNumber(serialNumber)
        .flatMap(
            jukeboxe ->
                Mono.just(
                    ApiResponse.<String>builder()
                        .timestamp(Instant.now())
                        .status(HttpStatus.CONFLICT.value())
                        .message("Jukebox déjà activé avec ID : " + jukeboxe.getJukeboxId())
                        .code("JUKBOX_ALREADY_EXISTS")
                        .data(jukeboxe.getJukeboxId())
                        .build()))
        .switchIfEmpty(
            generateUniqueJukeboxId()
                .flatMap(
                    newId -> {
                      Jukeboxe newJukeboxe =
                          Jukeboxe.builder().serialNumber(serialNumber).jukeboxId(newId).build();
                      return repository
                          .save(newJukeboxe)
                          .map(
                              saved ->
                                  ApiResponse.<String>builder()
                                      .timestamp(Instant.now())
                                      .status(HttpStatus.OK.value())
                                      .message("Jukebox activé avec succès")
                                      .code("SUCCESS")
                                      .data(saved.getJukeboxId())
                                      .build());
                    }));
  }

  private Mono<String> generateUniqueJukeboxId() {
    String candidateId = ID_PREFIX + generateRandomAlphanumeric(RANDOM_ID_LENGTH);
    log.info("id test {}", candidateId);
    return repository
        .findByJukeboxId(candidateId)
        .flatMap(existing -> generateUniqueJukeboxId()) // Collision, on recommence
        .switchIfEmpty(Mono.just(candidateId)); // ID unique, on le retourne
  }

  private String generateRandomAlphanumeric(int length) {
    StringBuilder sb = new StringBuilder(length);
    for (int i = 0; i < length; i++) {
      sb.append(ALPHANUM.charAt(random.nextInt(ALPHANUM.length())));
    }
    return sb.toString();
  }
}
