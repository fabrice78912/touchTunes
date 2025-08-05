package com.example.producer.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.Instant;

@Document("queue_entries")
public class QueueEntry {
    @Id
    private String id;
    private String userId;
    private String trackId;
    private String jukeboxId;
    private int position;
    private String priority = "STANDARD";
    private Instant addedAt = Instant.now();
}

