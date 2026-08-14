package com.adro.tms_backend.dto;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public record AiPlanDto(
        UUID id,
        UUID userId,
        Map<String, Object> planDetails,
        Instant createdAt
) {}
