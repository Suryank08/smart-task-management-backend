package com.adro.tms_backend.dto;

import com.adro.tms_backend.entity.enums.TaskPriority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;

public record TaskCreateRequest(
        UUID categoryId,
        @NotBlank @Size(max = 255) String title,
        String description,
        TaskPriority priority,
        Instant startDate,
        Instant dueDate,
        @Positive Integer estimatedMinutes,
        Set<UUID> tagIds) {}
