package com.caresphere.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Registration request. Self-registration always creates a {@code CLINICIAN} account;
 * administrators are bootstrapped separately, so the role is intentionally not selectable here.
 */
public record RegisterRequest(
        @NotBlank(message = "must not be blank")
        @Email(message = "must be a valid email address")
        String email,

        @NotBlank(message = "must not be blank")
        @Size(min = 8, max = 100, message = "must be between 8 and 100 characters")
        String password,

        @NotBlank(message = "must not be blank")
        @Size(max = 255, message = "must be at most 255 characters")
        String fullName
) {
}
