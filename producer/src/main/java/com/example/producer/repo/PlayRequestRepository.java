package com.example.producer.repo;

import com.example.producer.model.PlayRequest;
import java.util.List;
import java.util.Optional;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PlayRequestRepository extends MongoRepository<PlayRequest, String> {

  Optional<PlayRequest> findById(String jukeboxId);

  Optional<PlayRequest> findByUserId(String UserId);

  List<PlayRequest> findByTrackIdAndStatusIn(String trackId, List<String> statuses);
}
