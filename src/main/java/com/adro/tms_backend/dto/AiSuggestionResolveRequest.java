package com.adro.tms_backend.dto;

import com.adro.tms_backend.entity.enums.SuggestionStatus;
import jakarta.validation.constraints.NotNull;

public record AiSuggestionResolveRequest(@NotNull SuggestionStatus status) {}
