/*
package com.example.consumer.handler;

import com.example.common_lib.model.JukeboxEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

@Component
@RequiredArgsConstructor
public class JukeboxGlobalHandler {

    @Value("${jukebox.id}")
    private String assignedJukeboxId;


    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;

    private final Map<String, Queue<JukeboxEvent>> boostedQueues = new ConcurrentHashMap<>();
    private final Map<String, Queue<JukeboxEvent>> standardQueues = new ConcurrentHashMap<>();

    @PostConstruct
    public void initQueues() {
        boostedQueues.put(assignedJukeboxId, new ConcurrentLinkedQueue<>());
        standardQueues.put(assignedJukeboxId, new ConcurrentLinkedQueue<>());
    }

    @KafkaListener(
            topics = "jukebox-events",
            groupId = "${jukebox.group-id}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void onMessage(String key, Object value) {
        try {
            JukeboxEvent event = objectMapper.convertValue(value, JukeboxEvent.class);
            String jukeboxId = event.getJukeboxId();

            // Ne traite que les événements du jukebox assigné
            if (!assignedJukeboxId.equals(jukeboxId)) return;

            switch (event.getPriority()) {
                case BOOSTED -> handleBoosted(jukeboxId, event);
                case STANDARD -> handleStandard(jukeboxId, event);
            }

        } catch (Exception e) {
            System.err.println("❌ Erreur de traitement Kafka : " + e.getMessage());
        }
    }

    private void handleBoosted(String jukeboxId, JukeboxEvent event) {
        Queue<JukeboxEvent> queue = boostedQueues.get(jukeboxId);
        if (queue.size() < 50) {
            queue.add(event);
        } else {
            redisTemplate.opsForList().rightPush("overflow:boosted:" + jukeboxId, event);
        }
    }

    private void handleStandard(String jukeboxId, JukeboxEvent event) {
        Queue<JukeboxEvent> queue = standardQueues.get(jukeboxId);
        if (queue.size() < 50) {
            queue.add(event);
        } else {
            redisTemplate.opsForList().rightPush("overflow:standard:" + jukeboxId, event);
        }
    }
}
*/


package com.example.consumer.handler;

import com.example.common_lib.model.JukeboxEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

@Component
@RequiredArgsConstructor
public class JukeboxGlobalHandler {

    /*@Value("${jukebox.id}")
    private String assignedJukeboxId;

    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;

    // Files en mémoire par jukebox
    private final Map<String, Queue<JukeboxEvent>> boostedQueues = new ConcurrentHashMap<>();
    private final Map<String, Queue<JukeboxEvent>> standardQueues = new ConcurrentHashMap<>();

    *//**
     * Initialise les files en mémoire pour le jukebox assigné.
     *//*
    @PostConstruct
    public void initQueues() {
        boostedQueues.putIfAbsent(assignedJukeboxId, new ConcurrentLinkedQueue<>());
        standardQueues.putIfAbsent(assignedJukeboxId, new ConcurrentLinkedQueue<>());
    }

    *//**
     * Écoute les événements Kafka et les route vers la file appropriée.
     *//*
    @KafkaListener(
            topics = "jukebox-events",
            groupId = "${jukebox.group-id}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void onMessage(String key, Object value) {
        try {
            JukeboxEvent event = objectMapper.convertValue(value, JukeboxEvent.class);
            String jukeboxId = event.getJukeboxId();

            // Ignore les événements destinés à un autre jukebox
            if (!assignedJukeboxId.equals(jukeboxId)) return;

            // Route selon la priorité
            switch (event.getPriority()) {
                case BOOSTED -> handleEvent(jukeboxId, event, boostedQueues, "boosted");
                case STANDARD -> handleEvent(jukeboxId, event, standardQueues, "standard");
            }

        } catch (Exception e) {
            System.err.println("❌ Erreur de traitement Kafka : " + e.getMessage());
        }
    }

    *//**
     * Gère un événement pour une file donnée, avec débordement Redis si besoin.
     *
     * @param jukeboxId   L'identifiant du jukebox
     * @param event       L'événement à traiter
     * @param queues      La map des files (boosted ou standard)
     * @param redisPrefix Le préfixe à utiliser pour Redis en cas de débordement
     *//*
    private void handleEvent(
            String jukeboxId,
            JukeboxEvent event,
            Map<String, Queue<JukeboxEvent>> queues,
            String redisPrefix
    ) {
        Queue<JukeboxEvent> queue = queues.computeIfAbsent(jukeboxId, k -> new ConcurrentLinkedQueue<>());

        if (queue.size() < 50) {
            queue.add(event);
        } else {
            redisTemplate.opsForList().rightPush("overflow:" + redisPrefix + ":" + jukeboxId, event);
        }
    }*/
}

