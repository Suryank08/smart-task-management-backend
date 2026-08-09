package com.adro.tms_backend.dto;

import java.time.Instant;
import java.util.UUID;

public record ReminderDto(
        UUID id, UUID taskId, Instant remindAt, String channel, Instant sentAt, Instant createdAt) {}
