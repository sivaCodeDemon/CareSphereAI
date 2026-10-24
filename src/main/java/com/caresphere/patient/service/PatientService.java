package com.caresphere.patient.service;

import com.caresphere.auth.security.AuthenticatedUser;
import com.caresphere.common.audit.AuditService;
import com.caresphere.common.exception.ConflictException;
import com.caresphere.common.exception.ResourceNotFoundException;
import com.caresphere.patient.domain.Patient;
import com.caresphere.patient.dto.PatientRequest;
import com.caresphere.patient.dto.PatientResponse;
import com.caresphere.patient.repository.PatientRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class PatientService {

    private final PatientRepository patientRepository;
    private final AuthenticatedUser authenticatedUser;
    private final AuditService auditService;

    public PatientService(PatientRepository patientRepository, AuthenticatedUser authenticatedUser, AuditService auditService) {
        this.patientRepository = patientRepository;
        this.authenticatedUser = authenticatedUser;
        this.auditService = auditService;
    }

    public Page<PatientResponse> list(String search, Pageable pageable) {
        String q = (search == null || search.isBlank()) ? "" : search.trim();
        return patientRepository.search(q, pageable).map(PatientResponse::of);
    }

    public PatientResponse get(Long id) {
        return PatientResponse.of(requirePatient(id));
    }

    public PatientResponse create(PatientRequest request) {
        if (patientRepository.existsByMrn(request.mrn())) {
            throw new ConflictException("A patient with MRN " + request.mrn() + " already exists");
        }
        Patient patient = new Patient();
        applyTo(patient, request);
        patient.setCreatedBy(authenticatedUser.getUsername());
        Patient saved = patientRepository.save(patient);
        auditService.record("CREATE_PATIENT", "PATIENT", saved.getId(), saved.getId(), "SUCCESS");
        return PatientResponse.of(saved);
    }

    public PatientResponse update(Long id, PatientRequest request) {
        Patient patient = requirePatient(id);
        if (!patient.getMrn().equals(request.mrn()) && patientRepository.existsByMrn(request.mrn())) {
            throw new ConflictException("A patient with MRN " + request.mrn() + " already exists");
        }
        applyTo(patient, request);
        Patient saved = patientRepository.save(patient);
        auditService.record("UPDATE_PATIENT", "PATIENT", saved.getId(), saved.getId(), "SUCCESS");
        return PatientResponse.of(saved);
    }

    public void delete(Long id) {
        Patient patient = requirePatient(id);
        patientRepository.delete(patient);
        auditService.record("DELETE_PATIENT", "PATIENT", id, id, "SUCCESS");
    }

    private Patient requirePatient(Long id) {
        return patientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found: " + id));
    }

    private void applyTo(Patient patient, PatientRequest request) {
        patient.setMrn(request.mrn());
        patient.setFirstName(request.firstName());
        patient.setLastName(request.lastName());
        patient.setDateOfBirth(request.dateOfBirth());
        patient.setGender(request.gender());
        patient.setEmail(request.email());
        patient.setPhone(request.phone());
        patient.setAddress(request.address());
    }
}
