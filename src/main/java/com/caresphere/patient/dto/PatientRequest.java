package com.caresphere.patient.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

/**
 * Create/update payload for a patient.
 */
public record PatientRequest(
        @NotBlank(message = "must not be blank")
        @Size(max = 32, message = "must be at most 32 characters")
        String mrn,

        @NotBlank(message = "must not be blank")
        @Size(max = 100, message = "must be at most 100 characters")
        String firstName,

        @NotBlank(message = "must not be blank")
        @Size(max = 100, message = "must be at most 100 characters")
        String lastName,

        @Past(message = "must be a date in the past")
        LocalDate dateOfBirth,

        @Size(max = 16, message = "must be at most 16 characters")
        String gender,

        @Email(message = "must be a valid email address")
        String email,

        @Size(max = 32, message = "must be at most 32 characters")
        String phone,

        @Size(max = 500, message = "must be at most 500 characters")
        String address
) {
}
