package com.example.common_lib.model.exception;


public class ServiceUnavailableException1 extends RuntimeException {
    private final String code;
    private final String path;

    public ServiceUnavailableException1(String reason) {
        this(reason, null);
    }

    public ServiceUnavailableException1(String reason, String path) {
        super(reason);
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

