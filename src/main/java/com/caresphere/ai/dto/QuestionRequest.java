package com.caresphere.ai.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record QuestionRequest(
        @NotBlank(message = "must not be blank")
        @Size(max = 1000, message = "must be at most 1000 characters")
        String question,

        /** Optional: scope retrieval to a single patient's indexed documents. */
        Long patientId
) {
}
