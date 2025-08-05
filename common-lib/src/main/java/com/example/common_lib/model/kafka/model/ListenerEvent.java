package com.example.common_lib.model.kafka.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ListenerEvent<T> {
    private String entityId;
    private T payload;
    private String source;
    private int version;
    private String name;

}
