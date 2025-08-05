package com.example.producer.web;

import com.example.producer.mapper.JukeboxMapper;
import com.example.producer.model.Jukebox;
import com.example.producer.model.dto.JukeboxRequest;
import com.example.producer.service.nosql.JukeboxMongoService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/jukeboxes")
@RequiredArgsConstructor
public class JukeboxController {

  private final JukeboxMongoService jukeboxService;
  private final JukeboxMapper jukeboxMapper;

  @PostMapping
  public ResponseEntity<Jukebox> createJukebox(@RequestBody JukeboxRequest jukebox) {
    Jukebox created = jukeboxService.createJukebox(jukeboxMapper.toEntity(jukebox));
    return ResponseEntity.ok(created);
  }

  @GetMapping("/playing")
  public ResponseEntity<List<Jukebox>> getJukeboxesPlayingTrack(@RequestParam String title) {
    List<Jukebox> jukeboxes = jukeboxService.getJukeboxesPlayingTrack(title);
    return ResponseEntity.ok(jukeboxes);
  }
}
