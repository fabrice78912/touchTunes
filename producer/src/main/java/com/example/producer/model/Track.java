package com.example.producer.model;

import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document("tracks")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Track {
  @Id private String id;
  private String title;
  private String artist;
  private int durationSeconds;
  private String genre;
  private Instant createdAt = Instant.now();
}
