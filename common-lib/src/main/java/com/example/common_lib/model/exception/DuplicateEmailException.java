package com.example.common_lib.model.exception;


public class DuplicateEmailException extends RuntimeException {

    private final String code;

    public DuplicateEmailException(String message) {
        super(message);
        this.code = "EMAIL_EXISTS";
    }

    public String getCode() {
        return code;
    }
}

