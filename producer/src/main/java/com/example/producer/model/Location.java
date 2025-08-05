package com.example.producer.model;

import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document("locations")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Location {
  @Id private String id;
  private String name;
  private String address;
  private String city;
  private String country;
  private Instant createdAt = Instant.now();
}
