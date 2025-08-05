package com.example.producer.model;

import com.example.common_lib.model.EventType;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document("play_requests")
@AllArgsConstructor
@NoArgsConstructor
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
public class PlayRequest {
  @Id private java.lang.String id;
  private java.lang.String userId;
  private java.lang.String trackId;
  private java.lang.String jukeboxId;
  private Instant requestedAt = Instant.now();
  private java.lang.String status = "PENDING";
  private java.lang.String priority = "STANDARD";
  private EventType type; // PLAY_REQUEST, SKIP, STOP
}
