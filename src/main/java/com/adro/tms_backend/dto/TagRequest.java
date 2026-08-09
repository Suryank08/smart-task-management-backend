package com.adro.tms_backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record TagRequest(
        @NotBlank @Size(max = 30) String name,
        @Pattern(regexp = "^#[0-9A-Fa-f]{6}$") String color) {}
