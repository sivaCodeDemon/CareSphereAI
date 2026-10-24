package com.caresphere.patient.dto;

import com.caresphere.patient.domain.Patient;

import java.time.Instant;
import java.time.LocalDate;

public record PatientResponse(
        Long id,
        String mrn,
        String firstName,
        String lastName,
        LocalDate dateOfBirth,
        String gender,
        String email,
        String phone,
        String address,
        String createdBy,
        Instant createdAt,
        Instant updatedAt
) {
    public static PatientResponse of(Patient p) {
        return new PatientResponse(
                p.getId(), p.getMrn(), p.getFirstName(), p.getLastName(), p.getDateOfBirth(),
                p.getGender(), p.getEmail(), p.getPhone(), p.getAddress(), p.getCreatedBy(),
                p.getCreatedAt(), p.getUpdatedAt());
    }
}
