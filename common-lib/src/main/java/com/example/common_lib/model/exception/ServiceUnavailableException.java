package com.example.common_lib.model.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;


public class ServiceUnavailableException extends ResponseStatusException {
    private final String code;
    private final String path;

    public ServiceUnavailableException(String reason) {
        this(reason, null);
    }

    public ServiceUnavailableException(String reason, String path) {
        super(HttpStatus.SERVICE_UNAVAILABLE, reason);
        this.code = "SERVICE_UNAVAILABLE";
        this.path = path;
    }

    public String getCode() {
        return code;
    }

    public String getPath() {
        return path;
    }
}
