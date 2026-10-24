package com.caresphere.patient.repository;

import com.caresphere.patient.domain.MedicalRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MedicalRecordRepository extends JpaRepository<MedicalRecord, Long> {

    Page<MedicalRecord> findByPatientIdOrderByRecordedAtDesc(Long patientId, Pageable pageable);
}
