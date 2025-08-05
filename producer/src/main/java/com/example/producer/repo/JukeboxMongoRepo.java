package com.example.producer.repo;

import com.example.producer.model.Jukebox;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface JukeboxMongoRepo extends MongoRepository<Jukebox, String> {
    Optional<Jukebox> findById(String jukeboxId);
}



