package com.example.common_lib.model.response;


import com.example.common_lib.model.EventType;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    private Instant timestamp;
    private int status;
    private java.lang.String message;
    private java.lang.String code;
    private EventType eventName;
    private T data;
    private java.lang.String path;
}

