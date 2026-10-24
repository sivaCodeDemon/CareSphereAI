package com.caresphere.common.exception;

import java.time.Instant;
import java.util.List;

/**
 * Standard error envelope returned by every API failure. Keeps the client-facing shape
 * consistent and avoids leaking stack traces.
 *
 * @param timestamp when the error occurred
 * @param status    HTTP status code
 * @param error     short HTTP reason phrase
 * @param message   human-readable summary (safe to expose)
 * @param path      request path that triggered the error
 * @param details   optional field-level validation details
 */
public record ErrorResponse(
        Instant timestamp,
        int status,
        String error,
        String message,
        String path,
        List<String> details
) {
    public static ErrorResponse of(int status, String error, String message, String path, List<String> details) {
        return new ErrorResponse(Instant.now(), status, error, message, path, details == null ? List.of() : details);
    }
}
