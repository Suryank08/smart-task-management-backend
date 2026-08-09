package com.adro.tms_backend.dto;

import java.time.Instant;
import java.util.UUID;

public record CategoryDto(
        UUID id, UUID userId, String name, String icon, String color, Instant createdAt) {}
