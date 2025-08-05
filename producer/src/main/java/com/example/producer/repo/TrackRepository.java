package com.example.producer.repo;

import com.example.producer.model.Track;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TrackRepository extends MongoRepository<Track, String> {

    Optional<Track> findById(String jukeboxId);
}
