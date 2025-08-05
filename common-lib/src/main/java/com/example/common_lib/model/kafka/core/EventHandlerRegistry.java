package com.example.common_lib.model.kafka.core;

import com.example.common_lib.model.kafka.annotations.EventApiHandler;
import com.example.common_lib.model.kafka.annotations.EventApiHandlerClass;
import com.example.common_lib.model.kafka.model.ListenerEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.*;

@Component
public class EventHandlerRegistry implements BeanPostProcessor {

    private final Map<String, List<HandlerMethod>> handlerMap = new HashMap<>();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public List<HandlerMethod> getHandlers(String eventName, int version) {
        return handlerMap.getOrDefault(eventName + ":" + version, List.of());
    }

    /**
     * Dispatch un événement à tous les handlers enregistrés.
     *
     * @param entityId   l'ID de l'entité source de l'événement
     * @param eventName  le nom de l'événement
     * @param version    la version de l'événement
     * @param source     la source de l'événement (ex: service émetteur)
     * @param payloadJson la charge utile JSON de l'événement
     */
    public void dispatch(String entityId, String eventName, int version, String source, String payloadJson) {
        List<HandlerMethod> handlers = getHandlers(eventName, version);
        if (handlers.isEmpty()) {
            System.out.println("⚠️ Aucun handler trouvé pour l’événement : " + eventName + " v" + version);
            return;
        }

        for (HandlerMethod handler : handlers) {
            try {
                // Désérialiser le payload JSON en type attendu
                Object typedPayload = objectMapper.readValue(payloadJson, handler.payloadType());

                // Construire l'objet ListenerEvent complet avec tous les paramètres requis
                ListenerEvent<?> listenerEvent = new ListenerEvent<>(
                        entityId,
                        typedPayload,
                        source,
                        version,
                        eventName
                );

                // Appeler la méthode handler avec l'événement typé
                handler.method().invoke(handler.bean(), listenerEvent);

            } catch (Exception e) {
                System.err.println("❌ Erreur lors de l’appel du handler pour l’événement " + eventName + " v" + version);
                e.printStackTrace();
            }
        }
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) {
        if (!bean.getClass().isAnnotationPresent(EventApiHandlerClass.class)) return bean;

        for (Method method : bean.getClass().getDeclaredMethods()) {
            if (method.isAnnotationPresent(EventApiHandler.class)) {
                EventApiHandler annotation = method.getAnnotation(EventApiHandler.class);
                for (String name : annotation.eventNames()) {
                    for (int version : annotation.eventVersions()) {
                        String key = name + ":" + version;
                        handlerMap
                                .computeIfAbsent(key, k -> new ArrayList<>())
                                .add(new HandlerMethod(bean, method, annotation.payloadType()));
                    }
                }
            }
        }
        return bean;
    }

    public record HandlerMethod(Object bean, Method method, Class<?> payloadType) {}
}
