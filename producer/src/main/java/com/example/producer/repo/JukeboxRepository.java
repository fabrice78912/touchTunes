package com.example.producer.repo;

import com.example.producer.model.Jukeboxe;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

public interface JukeboxRepository extends ReactiveCrudRepository<Jukeboxe, Long> {
  Mono<Jukeboxe> findBySerialNumber(String serialNumber);

  Mono<Jukeboxe> findByJukeboxId(String candidateId);
}
