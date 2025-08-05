package com.example.common_lib.model.exception;

import lombok.Data;

@Data
public class NotFoundException extends RuntimeException {
    private final String code;
    private final String path;

    public NotFoundException(String message, String code, String path) {
        super(message);
        this.code = code;
        this.path = path;
    }
}

