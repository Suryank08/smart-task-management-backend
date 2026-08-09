package com.adro.tms_backend.exception;

import java.util.UUID;
import lombok.Getter;

@Getter
public class RecoverableAccountException extends RuntimeException {

    private final UUID userId;

    public RecoverableAccountException(UUID userId, String email) {
        super("An inactive account already exists for email: " + email);
        this.userId = userId;
    }
}
