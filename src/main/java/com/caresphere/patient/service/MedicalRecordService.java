package com.caresphere.patient.service;

import com.caresphere.common.exception.ResourceNotFoundException;
import com.caresphere.patient.domain.MedicalRecord;
import com.caresphere.patient.dto.MedicalRecordRequest;
import com.caresphere.patient.dto.MedicalRecordResponse;
import com.caresphere.patient.repository.MedicalRecordRepository;
import com.caresphere.patient.repository.PatientRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@Transactional
public class MedicalRecordService {

    private final MedicalRecordRepository medicalRecordRepository;
    private final PatientRepository patientRepository;

    public MedicalRecordService(MedicalRecordRepository medicalRecordRepository, PatientRepository patientRepository) {
        this.medicalRecordRepository = medicalRecordRepository;
        this.patientRepository = patientRepository;
    }

    public Page<MedicalRecordResponse> listByPatient(Long patientId, Pageable pageable) {
        requirePatient(patientId);
        return medicalRecordRepository.findByPatientIdOrderByRecordedAtDesc(patientId, pageable)
                .map(MedicalRecordResponse::of);
    }

    public MedicalRecordResponse create(Long patientId, MedicalRecordRequest request) {
        requirePatient(patientId);
        MedicalRecord record = new MedicalRecord();
        record.setPatientId(patientId);
        record.setTitle(request.title());
        record.setType(request.type());
        record.setContent(request.content());
        record.setRecordedAt(request.recordedAt() != null ? request.recordedAt() : Instant.now());
        return MedicalRecordResponse.of(medicalRecordRepository.save(record));
    }

    private void requirePatient(Long patientId) {
        if (!patientRepository.existsById(patientId)) {
            throw new ResourceNotFoundException("Patient not found: " + patientId);
        }
    }
}
