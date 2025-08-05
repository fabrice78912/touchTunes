package com.example.producer.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;

@Configuration
@Slf4j
public class MongoInitConfig {

  @Bean
  CommandLineRunner initCollections(MongoTemplate mongoTemplate) {
    return args -> {
      String[] collections = {
        "users", "tracks", "locations", "jukeboxes", "play_requests", "queue_entries"
      };

      for (String name : collections) {
        if (!mongoTemplate.collectionExists(name)) {
          mongoTemplate.createCollection(name);
          log.info("✅ Collection créée : {}", name);
        }
      }
    };
  }
}
