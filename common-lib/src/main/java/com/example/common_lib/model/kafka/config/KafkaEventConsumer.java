package com.example.common_lib.model.kafka.config;

import com.example.common_lib.model.kafka.core.EventHandlerRegistry;
import com.example.common_lib.model.kafka.model.ListenerEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class KafkaEventConsumer {

    private final EventHandlerRegistry eventHandlerRegistry;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "${eventapi.kafka.topic}", groupId = "${eventapi.kafka.groupId}")
    public void listen(ConsumerRecord<String, String> record) {
        String messageJson = record.value();
        try {
            // Désérialisation du ListenerEvent brut
            ListenerEvent<?> event = objectMapper.readValue(messageJson, ListenerEvent.class);

            // Dispatch avec tous les champs de l'événement
            eventHandlerRegistry.dispatch(
                    event.getEntityId(),
                    event.getName(),
                    event.getVersion(),
                    event.getSource(),
                    objectMapper.writeValueAsString(event.getPayload()) // JSON brut du payload
            );

        } catch (Exception e) {
            System.err.println("❌ Erreur de traitement du message Kafka : " + messageJson);
            e.printStackTrace();
        }
    }
}
