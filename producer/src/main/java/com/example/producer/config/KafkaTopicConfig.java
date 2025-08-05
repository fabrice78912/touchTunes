package com.example.producer.config;

import com.example.producer.utils.GlobalVariable;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
@RequiredArgsConstructor
public class KafkaTopicConfig {

  public static final String TOPIC_NAME = "jukebox-events";
  private final GlobalVariable globalVariable;

  @Bean
  public NewTopic createJukeboxEventsTopic() {
    return TopicBuilder.name(TOPIC_NAME)
        .partitions(globalVariable.getPartitionNumber())
        .replicas(1)
        .build();
  }
}
