package com.caresphere.patient.service;

import com.caresphere.auth.security.AuthenticatedUser;
import com.caresphere.common.exception.ConflictException;
import com.caresphere.common.exception.ResourceNotFoundException;
import com.caresphere.patient.domain.Patient;
import com.caresphere.patient.dto.PatientRequest;
import com.caresphere.patient.repository.PatientRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PatientServiceTest {

    @Mock private PatientRepository patientRepository;
    @Mock private AuthenticatedUser authenticatedUser;
    @Mock private com.caresphere.common.audit.AuditService auditService;

    private PatientService patientService;

    @BeforeEach
    void setUp() {
        patientService = new PatientService(patientRepository, authenticatedUser, auditService);
    }

    private PatientRequest request(String mrn) {
        return new PatientRequest(mrn, "Jane", "Doe", LocalDate.of(1990, 1, 1),
                "FEMALE", "jane@example.com", "555-1234", "1 Test St");
    }

    @Test
    void createPersistsPatientAndAttributesCreator() {
        when(patientRepository.existsByMrn("MRN-1")).thenReturn(false);
        when(authenticatedUser.getUsername()).thenReturn("clinician@caresphere.dev");
        when(patientRepository.save(any(Patient.class))).thenAnswer(inv -> {
            Patient p = inv.getArgument(0);
            p.setId(1L);
            return p;
        });

        var response = patientService.create(request("MRN-1"));

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.mrn()).isEqualTo("MRN-1");
        assertThat(response.createdBy()).isEqualTo("clinician@caresphere.dev");
    }

    @Test
    void createRejectsDuplicateMrn() {
        when(patientRepository.existsByMrn("MRN-1")).thenReturn(true);
        assertThatThrownBy(() -> patientService.create(request("MRN-1")))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    void getThrowsWhenPatientMissing() {
        when(patientRepository.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> patientService.get(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void listReturnsPage() {
        when(patientRepository.search(any(String.class), any())).thenReturn(Page.empty());
        var page = patientService.list("jane", PageRequest.of(0, 10));
        assertThat(page).isEmpty();
    }
}
