package com.caresphere.common.exception;

/**
 * Thrown when an uploaded document cannot be processed (corrupt PDF, unsupported type,
 * size limit exceeded). Mapped to HTTP 422 Unprocessable Entity.
 */
public class DocumentProcessingException extends RuntimeException {

    public DocumentProcessingException(String message) {
        super(message);
    }
}
