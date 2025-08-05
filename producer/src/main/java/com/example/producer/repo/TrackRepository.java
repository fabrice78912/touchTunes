package com.example.producer.repo;

import com.example.producer.model.Track;
import java.util.Optional;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TrackRepository extends MongoRepository<Track, String> {

  Optional<Track> findById(String jukeboxId);

  Optional<Track> findByTitleIgnoreCase(String title);
}
