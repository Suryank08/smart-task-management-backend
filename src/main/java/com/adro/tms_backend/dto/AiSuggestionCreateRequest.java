package com.adro.tms_backend.dto;

import com.adro.tms_backend.entity.enums.SuggestionType;
import jakarta.validation.constraints.NotNull;
import java.util.Map;

public record AiSuggestionCreateRequest(@NotNull SuggestionType type, @NotNull Map<String, Object> payload) {}
