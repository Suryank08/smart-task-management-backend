package com.adro.tms_backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.Map;

public record UserUpdateRequest(
        @NotBlank @Size(max = 100) String name,
        @Size(max = 500) String avatarUrl,
        @Size(max = 50) String timezone,
        Map<String, Object> preferences) {}
