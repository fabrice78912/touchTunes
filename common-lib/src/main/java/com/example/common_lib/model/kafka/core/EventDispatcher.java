package com.example.common_lib.model.kafka.core;

import com.example.common_lib.model.kafka.model.ListenerEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

@Component
public class EventDispatcher {

    private final EventHandlerRegistry registry;
    private final ObjectMapper objectMapper;

    public EventDispatcher(EventHandlerRegistry registry) {
        this.registry = registry;
        this.objectMapper = new ObjectMapper();
    }

    public void dispatch(String messageJson) {
        try {
            // Désérialisation générique du message
            ListenerEvent<?> rawEvent = objectMapper.readValue(messageJson, ListenerEvent.class);

            var handlers = registry.getHandlers(rawEvent.getName(), rawEvent.getVersion());

            for (var handler : handlers) {
                // Conversion du payload en type attendu par le handler
                Object typedPayload = objectMapper.convertValue(rawEvent.getPayload(), handler.payloadType());

                // Création de l'objet typé
                ListenerEvent<Object> typedEvent = new ListenerEvent<>(
                        rawEvent.getEntityId(),
                        typedPayload,
                        rawEvent.getSource(),
                        rawEvent.getVersion(),
                        rawEvent.getName()
                );

                // Appel de la méthode de traitement
                handler.method().invoke(handler.bean(), typedEvent);
            }

        } catch (Exception e) {
            System.err.println("❌ Erreur lors du dispatch de l’événement : " + e.getMessage());
            e.printStackTrace();
        }
    }
}
