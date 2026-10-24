package com.caresphere.document.repository;

import com.caresphere.document.domain.DocumentEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DocumentRepository extends JpaRepository<DocumentEntity, Long> {

    Page<DocumentEntity> findByPatientIdOrderByCreatedAtDesc(Long patientId, Pageable pageable);
}
