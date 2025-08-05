package com.example.consumer.client;

import java.lang.reflect.Field;

public class TestUtils {
    public static void setField(Object target, String fieldName, Object value) {
        try {
            Field field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de l'injection du mock : " + fieldName, e);
        }
    }
}

