package com.adro.tms_backend.dto;

import com.adro.tms_backend.entity.enums.RecurrenceFrequency;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record RecurringPatternDto(
        UUID id,
        UUID taskId,
        RecurrenceFrequency frequency,
        int intervalCount,
        List<Integer> daysOfWeek,
        Instant endDate,
        Instant createdAt) {}
