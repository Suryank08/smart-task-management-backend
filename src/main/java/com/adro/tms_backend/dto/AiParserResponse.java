package com.adro.tms_backend.dto;

import java.time.Instant;
import java.util.List;
public record AiParserResponse(
    String title,
    String description,
    String priority,
    Instant dueDate,
    Integer estimatedMinutes,
    List<String> subtasks
) {}
