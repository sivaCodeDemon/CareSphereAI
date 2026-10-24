package com.caresphere.patient.controller;

import com.caresphere.patient.dto.MedicalRecordRequest;
import com.caresphere.patient.dto.MedicalRecordResponse;
import com.caresphere.patient.service.MedicalRecordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Medical Records", description = "Clinical records attached to a patient")
@RestController
@RequestMapping("/api/patients/{patientId}/records")
public class MedicalRecordController {

    private final MedicalRecordService medicalRecordService;

    public MedicalRecordController(MedicalRecordService medicalRecordService) {
        this.medicalRecordService = medicalRecordService;
    }

    @Operation(summary = "List a patient's medical records (paginated)")
    @GetMapping
    public Page<MedicalRecordResponse> list(@PathVariable Long patientId, Pageable pageable) {
        return medicalRecordService.listByPatient(patientId, pageable);
    }

    @Operation(summary = "Add a medical record to a patient")
    @PostMapping
    public ResponseEntity<MedicalRecordResponse> create(@PathVariable Long patientId,
                                                      @Valid @RequestBody MedicalRecordRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(medicalRecordService.create(patientId, request));
    }
}
