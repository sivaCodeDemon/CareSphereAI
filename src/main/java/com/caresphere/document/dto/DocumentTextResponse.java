package com.caresphere.document.dto;

import com.caresphere.document.domain.DocumentEntity;

public record DocumentTextResponse(Long id, Long patientId, int pageCount, String text) {
    public static DocumentTextResponse of(DocumentEntity d) {
        return new DocumentTextResponse(d.getId(), d.getPatientId(),
                d.getPageCount() == null ? 0 : d.getPageCount(),
                d.getExtractedText() == null ? "" : d.getExtractedText());
    }
}
