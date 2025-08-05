package com.example.consumer.dto;

import com.example.common_lib.model.EventType;
import com.example.common_lib.model.PriorityLevel;
import lombok.Data;

@Data
public class JukeboxEventPayload {
    private String userId;
    private String trackId;
    private String jukeboxId;
    private EventType eventType;
    private PriorityLevel priority;
}

