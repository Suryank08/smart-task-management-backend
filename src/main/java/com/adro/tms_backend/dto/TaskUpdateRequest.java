package com.adro.tms_backend.dto;

import com.adro.tms_backend.entity.enums.TaskPriority;
import com.adro.tms_backend.entity.enums.TaskStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;

public record TaskUpdateRequest(
        UUID categoryId,
        @NotBlank @Size(max = 255) String title,
        String description,
        TaskStatus status,
        TaskPriority priority,
        Instant startDate,
        Instant dueDate,
        @Positive Integer estimatedMinutes,
        @PositiveOrZero Integer actualMinutes,
        boolean pinned,
        Instant reminderAt,
        Set<UUID> tagIds) {}
