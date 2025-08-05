package com.example.producer.repo;

import com.example.producer.model.PlayRequest;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PlayRequestRepository extends MongoRepository<PlayRequest, String> {

    Optional<PlayRequest> findById(String jukeboxId);
    Optional<PlayRequest> findByUserId(String UserId);
}
