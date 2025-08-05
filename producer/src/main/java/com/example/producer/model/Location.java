package com.example.producer.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.Instant;

@Document("locations")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Location {
    @Id
    private String id;
    private String name;
    private String address;
    private String city;
    private String country;
    private Instant createdAt = Instant.now();
}

