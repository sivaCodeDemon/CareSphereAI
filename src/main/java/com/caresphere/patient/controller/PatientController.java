package com.caresphere.patient.controller;

import com.caresphere.patient.dto.PatientRequest;
import com.caresphere.patient.dto.PatientResponse;
import com.caresphere.patient.service.PatientService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Patients", description = "Patient registration, profiles, and management")
@RestController
@RequestMapping("/api/patients")
public class PatientController {

    private final PatientService patientService;

    public PatientController(PatientService patientService) {
        this.patientService = patientService;
    }

    @Operation(summary = "List patients (paginated, with optional name/MRN search)")
    @GetMapping
    public Page<PatientResponse> list(
            @Parameter(description = "Search across first name, last name, and MRN") @RequestParam(required = false) String search,
            Pageable pageable) {
        return patientService.list(search, pageable);
    }

    @Operation(summary = "Get a patient by id")
    @GetMapping("/{id}")
    public PatientResponse get(@PathVariable Long id) {
        return patientService.get(id);
    }

    @Operation(summary = "Register a new patient")
    @PostMapping
    public ResponseEntity<PatientResponse> create(@Valid @RequestBody PatientRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(patientService.create(request));
    }

    @Operation(summary = "Update an existing patient")
    @PutMapping("/{id}")
    public PatientResponse update(@PathVariable Long id, @Valid @RequestBody PatientRequest request) {
        return patientService.update(id, request);
    }

    @Operation(summary = "Delete a patient")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        patientService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
