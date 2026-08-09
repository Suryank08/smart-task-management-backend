package com.adro.tms_backend.dto;

import java.time.Instant;
import java.util.UUID;

public record AttachmentDto(
        UUID id,
        UUID taskId,
        UUID userId,
        String fileName,
        String fileUrl,
        String mimeType,
        Integer fileSizeKb,
        Instant uploadedAt) {}
