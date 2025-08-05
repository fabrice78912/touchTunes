package com.example.common_lib.model.exception;

import com.example.common_lib.model.response.ApiResponse;
import io.r2dbc.spi.R2dbcDataIntegrityViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler({R2dbcDataIntegrityViolationException.class, DuplicateKeyException.class})
    public ResponseEntity<ApiResponse<Object>> handleR2dbcDuplicateKey(Exception ex) {
        String message = ex.getMessage() != null && ex.getMessage().contains("Duplicate entry")
                ? "Ce numéro de série existe déjà dans la base de données."
                : "Erreur base de données : " + ex.getMessage();

        String code = ex.getMessage() != null && ex.getMessage().contains("Duplicate entry")
                ? "SERIAL_NUMBER_EXISTS"
                : "DB_ERROR";

        int status = ex.getMessage() != null && ex.getMessage().contains("Duplicate entry") ? 409 : 500;

        ApiResponse<Object> response = ApiResponse.builder()
                .timestamp(Instant.now())
                .status(status)
                .message(message)
                .code(code)
                .build();

        return ResponseEntity.status(status).body(response);
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ApiResponse<Object>> handleIllegalState(IllegalStateException ex) {
        ApiResponse<Object> response = ApiResponse.builder()
                .timestamp(Instant.now())
                .status(HttpStatus.CONFLICT.value())
                .message(ex.getMessage())
                .code("SERIAL_NUMBER_EXISTS")
                .build();

        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    // Gère les autres exceptions globalement
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Object>> handleOtherExceptions(Exception ex) {
        ApiResponse<Object> response = ApiResponse.builder()
                .timestamp(Instant.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .message("Bad request : " + ex.getMessage())
                .code("BAD_REQUEST")
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ApiResponse<Object>> handleNotFoundException(NotFoundException ex) {
        log.error("NotFoundException capturée: {}", ex.getMessage()); // <== ajoute ce log
        ApiResponse<Object> response = ApiResponse.builder()
                .timestamp(Instant.now())
                .status(HttpStatus.NOT_FOUND.value())
                .message(ex.getMessage())
                .code(ex.getCode())
                .path(ex.getPath())
                .build();

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }


}
