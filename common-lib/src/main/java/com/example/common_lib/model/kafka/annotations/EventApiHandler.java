package com.example.common_lib.model.kafka.annotations;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface EventApiHandler {
    String[] eventNames();
    Class<?> payloadType();
    int[] eventVersions() default {1};
}
