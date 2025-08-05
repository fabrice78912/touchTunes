package com.example.producer.service.nosql;


import com.example.producer.model.Jukebox;
import com.example.producer.repo.JukeboxMongoRepo;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class JukeboxMongoService {

    private final JukeboxMongoRepo jukeboxRepository;

    public Jukebox createJukebox(Jukebox jukebox) {
        jukebox.setCreatedAt(Instant.now());
        return jukeboxRepository.save(jukebox);
    }
}

