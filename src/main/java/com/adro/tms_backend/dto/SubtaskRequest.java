package com.adro.tms_backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record SubtaskRequest(
        @NotBlank @Size(max = 255) String title, boolean completed, @PositiveOrZero int position) {}
