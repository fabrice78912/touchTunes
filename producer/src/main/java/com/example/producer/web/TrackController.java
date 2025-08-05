package com.example.producer.web;

import com.example.producer.model.Track;
import com.example.producer.model.dto.TrackRequestDto;
import com.example.producer.service.nosql.TrackService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tracks")
@RequiredArgsConstructor
public class TrackController {

  private final TrackService trackService;
  private final ModelMapper modelMapper;

  @PostMapping
  public ResponseEntity<Track> createTrack(@RequestBody TrackRequestDto trackRequestDto) {
    // Mapper TrackRequestDto en Track
    Track track = modelMapper.map(trackRequestDto, Track.class);
    return ResponseEntity.ok(trackService.createTrack(track));
  }
}
