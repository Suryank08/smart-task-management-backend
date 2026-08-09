package com.adro.tms_backend.dto;

import com.adro.tms_backend.entity.enums.RecurrenceFrequency;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.time.Instant;
import java.util.List;

public record RecurringPatternRequest(
        @NotNull RecurrenceFrequency frequency,
        @Positive int intervalCount,
        List<Integer> daysOfWeek,
        Instant endDate) {}
