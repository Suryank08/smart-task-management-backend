package com.adro.tms_backend.dto;

import com.adro.tms_backend.entity.enums.SuggestionStatus;
import com.adro.tms_backend.entity.enums.SuggestionType;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public record AiSuggestionDto(
        UUID id,
        UUID taskId,
        SuggestionType type,
        Map<String, Object> payload,
        SuggestionStatus status,
        Instant createdAt,
        Instant resolvedAt) {}
