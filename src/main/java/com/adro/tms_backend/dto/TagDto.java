package com.adro.tms_backend.dto;

import java.time.Instant;
import java.util.UUID;

public record TagDto(UUID id, UUID userId, String name, String color, Instant createdAt) {}
