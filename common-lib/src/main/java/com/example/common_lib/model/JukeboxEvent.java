package com.example.common_lib.model;

import java.time.Instant;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class JukeboxEvent {
    private java.lang.String eventId;
    private java.lang.String jukeboxId;
    private EventType type;
    private PriorityLevel priority;
    private Instant timestamp= Instant.now();
    private Map<java.lang.String, Object> payload;
}
