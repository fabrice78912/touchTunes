package com.example.producer.repo;

import com.example.producer.model.Jukebox;
import java.util.List;
import java.util.Optional;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JukeboxMongoRepo extends MongoRepository<Jukebox, String> {
  Optional<Jukebox> findById(String jukeboxId);

  List<Jukebox> findByIdIn(List<String> ids);
}
