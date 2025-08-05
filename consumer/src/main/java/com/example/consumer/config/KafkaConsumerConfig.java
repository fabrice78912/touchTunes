package com.example.consumer.config;

import org.apache.kafka.common.TopicPartition;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.KafkaMessageListenerContainer;
import org.springframework.kafka.listener.MessageListener;

@Configuration
public class KafkaConsumerConfig {

   /* @Value("${jukebox.id}")
    private String jukeboxId;

    @Value("${jukebox.topic}")
    private String topicName;

    // Correspond au nombre réel de partitions du topic Kafka
    private static final int TOTAL_PARTITIONS = 5;

    @Bean
    public KafkaMessageListenerContainer<String, String> dedicatedJukeboxListenerContainer(
            ConsumerFactory<String, String> consumerFactory,
            MessageListener<String, String> messageListener
    ) {
        int partition = Math.abs(jukeboxId.hashCode()) % TOTAL_PARTITIONS;
        TopicPartition topicPartition = new TopicPartition(topicName, partition);

        // 👇 Utilise le bon constructeur ici
        ContainerProperties containerProps = new ContainerProperties(topicPartition);

        containerProps.setMessageListener(messageListener);
        containerProps.setAckMode(ContainerProperties.AckMode.MANUAL_IMMEDIATE);
        containerProps.setGroupId("jukebox-consumer-" + jukeboxId);

        System.out.printf("🎧 [%s] écoute uniquement la partition [%d] du topic [%s]%n", jukeboxId, partition, topicName);

        return new KafkaMessageListenerContainer<>(consumerFactory, containerProps);
    }*/
}
