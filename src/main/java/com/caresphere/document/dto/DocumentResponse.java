package com.caresphere.document.dto;

import com.caresphere.document.domain.DocumentEntity;

import java.time.Instant;

public record DocumentResponse(
        Long id,
        Long patientId,
        String filename,
        String contentType,
        long sizeBytes,
        int pageCount,
        String status,
        String uploadedBy,
        Instant createdAt
) {
    public static DocumentResponse of(DocumentEntity d) {
        return new DocumentResponse(
                d.getId(), d.getPatientId(), d.getFilename(), d.getContentType(), d.getSizeBytes(),
                d.getPageCount() == null ? 0 : d.getPageCount(), d.getStatus().name(),
                d.getUploadedBy(), d.getCreatedAt());
    }
}
