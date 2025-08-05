package com.example.producer.web;


import com.example.producer.model.Jukebox;
import com.example.producer.service.nosql.JukeboxMongoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/jukeboxes")
@RequiredArgsConstructor
public class JukeboxController {

    private final JukeboxMongoService jukeboxService;

    @PostMapping
    public ResponseEntity<Jukebox> createJukebox(@RequestBody Jukebox jukebox) {
        Jukebox created = jukeboxService.createJukebox(jukebox);
        return ResponseEntity.ok(created);
    }
}

