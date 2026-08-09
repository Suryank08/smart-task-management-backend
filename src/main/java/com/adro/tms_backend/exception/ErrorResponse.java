package com.adro.tms_backend.exception;

import java.time.Instant;
import java.util.Map;

public record ErrorResponse(Instant timestamp, int status, String error, String message, Map<String, String> errors) {

    public ErrorResponse(int status, String error, String message) {
        this(Instant.now(), status, error, message, null);
    }

    public ErrorResponse(int status, String error, String message, Map<String, String> errors) {
        this(Instant.now(), status, error, message, errors);
    }
}
