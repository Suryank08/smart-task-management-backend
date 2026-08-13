package com.adro.tms_backend.dto;

import jakarta.validation.constraints.NotBlank;

public record AiParserRequest(
    @NotBlank(message = "Text is required")
    String text
) {}
