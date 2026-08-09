package com.adro.tms_backend.dto;

import com.adro.tms_backend.entity.enums.UserRole;
import com.adro.tms_backend.entity.enums.UserStatus;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public record UserDto(
        UUID id,
        String email,
        String name,
        String avatarUrl,
        UserRole role,
        UserStatus status,
        String timezone,
        Map<String, Object> preferences,
        Instant createdAt,
        Instant updatedAt) {}
