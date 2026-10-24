package com.caresphere.patient.dto;

import com.caresphere.patient.domain.MedicalRecord;

import java.time.Instant;

public record MedicalRecordResponse(
        Long id,
        Long patientId,
        String title,
        String type,
        String content,
        Instant recordedAt,
        Instant createdAt,
        Instant updatedAt
) {
    public static MedicalRecordResponse of(MedicalRecord r) {
        return new MedicalRecordResponse(
                r.getId(), r.getPatientId(), r.getTitle(), r.getType().name(), r.getContent(),
                r.getRecordedAt(), r.getCreatedAt(), r.getUpdatedAt());
    }
}
