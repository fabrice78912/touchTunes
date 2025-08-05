package com.example.producer.service.nosql;

import com.example.common_lib.model.exception.NotFoundException;
import com.example.producer.model.Jukebox;
import com.example.producer.model.PlayRequest;
import com.example.producer.model.Track;
import com.example.producer.repo.JukeboxMongoRepo;
import com.example.producer.repo.PlayRequestRepository;
import com.example.producer.repo.TrackRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class JukeboxMongoService {

  private final JukeboxMongoRepo jukeboxRepository;
  private final TrackRepository trackRepository;
  private final PlayRequestRepository playRequestRepository;
  private final MongoTemplate mongoTemplate;
  private final ObjectMapper objectMapper;

  public Jukebox createJukebox(Jukebox jukebox) {
    jukebox.setCreatedAt(Instant.now());
    return jukeboxRepository.save(jukebox);
  }

  public List<Jukebox> getJukeboxesPlayingTrack(String trackTitle) {
    Optional<Track> trackOpt = trackRepository.findByTitleIgnoreCase(trackTitle);
    if (trackOpt.isEmpty()) {
      throw new NotFoundException( trackTitle + " Non trouve ", "404", "");
    }

    Track track = trackOpt.get();
    List<String> statuses = List.of("PENDING", "QUEUED", "PLAYING");
    List<PlayRequest> playRequests =
        playRequestRepository.findByTrackIdAndStatusIn(track.getId(), statuses);

    List<String> jukeboxIds =
        playRequests.stream().map(PlayRequest::getJukeboxId).distinct().toList();

    return jukeboxRepository.findByIdIn(jukeboxIds);
  }
}
