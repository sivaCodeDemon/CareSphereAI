package com.caresphere.patient.dto;

import com.caresphere.patient.domain.RecordType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.Instant;

public record MedicalRecordRequest(
        @NotBlank(message = "must not be blank")
        @Size(max = 255, message = "must be at most 255 characters")
        String title,

        @NotNull(message = "must not be null")
        RecordType type,

        @NotBlank(message = "must not be blank")
        String content,

        Instant recordedAt
) {
}
