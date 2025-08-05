package com.example.producer.model;

import java.time.Instant;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document("jukeboxes")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Builder
public class Jukebox {
  @Id private String id;
  private String serialNumber;
  private String jukeboxId;
  private String status = "ACTIVE";
  private String locationId; // Référence vers Location (par son ID)
  private Instant lastHeartbeat;
  private Instant createdAt = Instant.now();
  private String model;
}
