package com.adro.tms_backend.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.Instant;

public record ReminderRequest(@NotNull Instant remindAt, @Size(max = 20) String channel) {}
