package com.adro.tms_backend.dto;

import java.time.Instant;
import java.util.UUID;

public record SubtaskDto(
        UUID id,
        UUID taskId,
        String title,
        boolean completed,
        int position,
        Instant createdAt,
        Instant updatedAt) {}
