package com.example.producer.model;

import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document("jukeboxes")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Jukebox {
  @Id private String id;
  private String serialNumber;
  private String jukeboxId;
  private String status = "ACTIVE";
  private String locationId; // Référence vers Location (par son ID)
  private Instant lastHeartbeat;
  private Instant createdAt = Instant.now();
}
