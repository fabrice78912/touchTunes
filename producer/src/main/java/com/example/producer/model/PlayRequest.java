package com.example.producer.model;

import com.example.common_lib.model.EventType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.Instant;

@Document("play_requests")
@AllArgsConstructor
@NoArgsConstructor
@Data
public class PlayRequest {
    @Id
    private java.lang.String id;
    private java.lang.String userId;
    private java.lang.String trackId;
    private java.lang.String jukeboxId;
    private Instant requestedAt = Instant.now();
    private java.lang.String status = "PENDING";
    private java.lang.String priority = "STANDARD";
    private EventType type; // PLAY_REQUEST, SKIP, STOP
}

