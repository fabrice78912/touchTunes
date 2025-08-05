package com.example.producer.service.nosql;

import com.example.producer.model.Track;
import com.example.producer.repo.TrackRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TrackService {

  private final TrackRepository trackRepository;

  public Track createTrack(Track track) {
    track.setCreatedAt(java.time.Instant.now());
    return trackRepository.save(track);
  }
}
