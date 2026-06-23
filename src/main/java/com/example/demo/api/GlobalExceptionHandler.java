package com.example.demo.api;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.async.AsyncRequestTimeoutException;
import jakarta.servlet.http.HttpServletResponse;

import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AsyncRequestTimeoutException.class)
    public void obsluzTimeout(AsyncRequestTimeoutException ex) {
        // Ciche zamknięcie połączenia przy timeoutcie SSE (zapobiega próbom zapisu JSON-a)
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> obsluzIllegalArgument(IllegalArgumentException ex, HttpServletResponse response) {
        if (response.isCommitted()) {
            return null;
        }
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(Map.of(
                        "status", "error",
                        "message", ex.getMessage()
                ));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> obsluzGeneric(Exception ex, HttpServletResponse response) {
        if (response.isCommitted()) {
            // Połączenie zostało już rozpoczęte (np. SSE), nie możemy zmienić statusu ani wysłać JSON-a
            return null;
        }
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of(
                        "status", "error",
                        "message", "Wystąpił nieoczekiwany błąd serwera: " + ex.getMessage()
                ));
    }
}
