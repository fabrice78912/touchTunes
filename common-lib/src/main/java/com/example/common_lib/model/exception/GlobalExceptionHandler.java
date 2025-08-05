package com.example.common_lib.model.exception;

import com.example.common_lib.model.response.ApiResponse;
import com.example.common_lib.model.response.ApiResponse1;
import io.r2dbc.spi.R2dbcDataIntegrityViolationException;
import jakarta.servlet.http.HttpServletRequest;
import java.time.Instant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ServerWebInputException;
import reactor.core.publisher.Mono;

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


    @ExceptionHandler(ServiceUnavailableException.class)
    public ResponseEntity<ApiResponse<Object>> handleServiceUnavailable(ServiceUnavailableException ex, HttpServletRequest request) {
        log.error("ServiceUnavailableException capturée: {}", ex.getReason());

        ApiResponse<Object> response = ApiResponse.builder()
                .timestamp(Instant.now())
                .status(HttpStatus.SERVICE_UNAVAILABLE.value())
                .message(ex.getReason())
                .code("SERVICE_UNAVAILABLE")
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response);
    }

    @ExceptionHandler(ServiceUnavailableException1.class)
    public Mono<ResponseEntity<ApiResponse1<Void>>> handleServiceUnavailable(ServiceUnavailableException1 ex) {
        ApiResponse1<Void> response = ApiResponse1.error(
                ex.getMessage(),
                ex.getCode(),
                HttpStatus.SERVICE_UNAVAILABLE.value()
        );
        return Mono.just(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response));
    }

    // Paramètre mal typé ou manquant
    @ExceptionHandler({ServerWebInputException.class, MethodArgumentTypeMismatchException.class})
    public Mono<ResponseEntity<ApiResponse1<Void>>> handleInvalidParam(Exception ex) {
        ApiResponse1<Void> response = ApiResponse1.error(
                "Mauvaise requête",
                "BAD_REQUEST",
                HttpStatus.BAD_REQUEST.value()
        );
        return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response));
    }

    // Exceptions génériques
    @ExceptionHandler(Exception.class)
    public Mono<ResponseEntity<ApiResponse1<Void>>> handleGeneric(Exception ex) {
        log.error("Exception capturée: {}", ex.getMessage(), ex);
        ApiResponse1<Void> response = ApiResponse1.error(
                "Erreur interne",
                "INTERNAL_ERROR",
                HttpStatus.INTERNAL_SERVER_ERROR.value()
        );
        return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response));
    }

    @ExceptionHandler(DuplicateEmailException.class)
    public ResponseEntity<ApiResponse<Object>> handleDuplicateEmail(DuplicateEmailException ex) {
        ApiResponse<Object> response = ApiResponse.builder()
                .timestamp(Instant.now())
                .status(HttpStatus.CONFLICT.value())
                .message(ex.getMessage())
                .code(ex.getCode())
                .build();

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

}
