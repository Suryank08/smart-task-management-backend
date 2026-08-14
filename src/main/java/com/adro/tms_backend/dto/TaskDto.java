package com.adro.tms_backend.dto;

import com.adro.tms_backend.entity.enums.TaskPriority;
import com.adro.tms_backend.entity.enums.TaskStatus;
import java.time.Instant;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public record TaskDto(
        UUID id,
        UUID userId,
        UUID categoryId,
        String title,
        String description,
        TaskStatus status,
        TaskPriority priority,
        Instant startDate,
        Instant dueDate,
        Instant completedAt,
        Integer estimatedMinutes,
        boolean pinned,
        Set<UUID> tagIds,
        Instant reminderAt,
        Instant createdAt,
        Instant updatedAt) {}
