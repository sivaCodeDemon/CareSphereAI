package com.caresphere.document.service;

import com.caresphere.auth.security.AuthenticatedUser;
import com.caresphere.common.audit.AuditService;
import com.caresphere.common.exception.DocumentProcessingException;
import com.caresphere.common.exception.ResourceNotFoundException;
import com.caresphere.document.domain.DocumentEntity;
import com.caresphere.document.domain.DocumentStatus;
import com.caresphere.document.dto.DocumentResponse;
import com.caresphere.document.dto.DocumentTextResponse;
import com.caresphere.document.extract.ExtractedPdf;
import com.caresphere.document.extract.PdfTextExtractor;
import com.caresphere.document.repository.DocumentRepository;
import com.caresphere.document.storage.DocumentStorage;
import com.caresphere.patient.repository.PatientRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
@Transactional
public class DocumentService {

    private static final Logger log = LoggerFactory.getLogger(DocumentService.class);

    private final DocumentRepository documentRepository;
    private final PatientRepository patientRepository;
    private final DocumentStorage storage;
    private final PdfTextExtractor extractor;
    private final AuthenticatedUser authenticatedUser;
    private final AuditService auditService;
    private final long maxFileBytes;

    public DocumentService(DocumentRepository documentRepository, PatientRepository patientRepository,
                           DocumentStorage storage, PdfTextExtractor extractor, AuthenticatedUser authenticatedUser,
                           AuditService auditService,
                           @Value("${caresphere.storage.max-file-size-mb:10}") long maxFileSizeMb) {
        this.documentRepository = documentRepository;
        this.patientRepository = patientRepository;
        this.storage = storage;
        this.extractor = extractor;
        this.authenticatedUser = authenticatedUser;
        this.auditService = auditService;
        this.maxFileBytes = maxFileSizeMb * 1024 * 1024;
    }

    public DocumentResponse upload(Long patientId, MultipartFile file) {
        requirePatient(patientId);
        validate(file);

        String storageKey;
        try {
            storageKey = storage.store(patientId, file);
        } catch (IOException e) {
            log.error("Failed to store uploaded file for patient {}: {}", patientId, e.getMessage());
            throw new DocumentProcessingException("Failed to store uploaded file");
        }

        DocumentEntity doc = new DocumentEntity();
        doc.setPatientId(patientId);
        doc.setFilename(file.getOriginalFilename());
        doc.setContentType(file.getContentType());
        doc.setSizeBytes(file.getSize());
        doc.setStorageKey(storageKey);
        doc.setStatus(DocumentStatus.PENDING);
        doc.setUploadedBy(authenticatedUser.getUsername());
        doc = documentRepository.save(doc);

        try {
            ExtractedPdf extracted = extractor.extract(file.getBytes());
            doc.setExtractedText(extracted.fullText());
            doc.setPageCount(extracted.pageCount());
            doc.setStatus(DocumentStatus.EXTRACTED);
            documentRepository.save(doc);
            auditService.record("UPLOAD_DOCUMENT", "DOCUMENT", doc.getId(), patientId, "SUCCESS");
            return DocumentResponse.of(doc);
        } catch (Exception e) {
            log.error("PDF text extraction failed for document {}: {}", doc.getId(), e.getMessage());
            doc.setStatus(DocumentStatus.FAILED);
            documentRepository.save(doc);
            auditService.record("UPLOAD_DOCUMENT", "DOCUMENT", doc.getId(), patientId, "FAILURE");
            throw new DocumentProcessingException("Failed to extract text from PDF");
        }
    }

    public Page<DocumentResponse> list(Long patientId, Pageable pageable) {
        requirePatient(patientId);
        return documentRepository.findByPatientIdOrderByCreatedAtDesc(patientId, pageable).map(DocumentResponse::of);
    }

    public DocumentResponse get(Long patientId, Long documentId) {
        return DocumentResponse.of(requireOwnedDocument(patientId, documentId));
    }

    public DocumentTextResponse getText(Long patientId, Long documentId) {
        return DocumentTextResponse.of(requireOwnedDocument(patientId, documentId));
    }

    private void validate(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new DocumentProcessingException("File is required");
        }
        String contentType = file.getContentType();
        boolean looksLikePdf = "application/pdf".equalsIgnoreCase(contentType)
                || "application/octet-stream".equalsIgnoreCase(contentType)
                || file.getOriginalFilename() != null && file.getOriginalFilename().toLowerCase().endsWith(".pdf");
        if (!looksLikePdf) {
            try {
                byte[] header = file.getBytes();
                looksLikePdf = header.length >= 4 && header[0] == 0x25 && header[1] == 0x50 && header[2] == 0x44 && header[3] == 0x46;
            } catch (IOException e) {
                throw new DocumentProcessingException("Unable to validate PDF file");
            }
        }
        if (!looksLikePdf) {
            throw new DocumentProcessingException("Only PDF files are accepted");
        }
        if (file.getSize() > maxFileBytes) {
            throw new DocumentProcessingException("File exceeds the maximum allowed size");
        }
    }

    private void requirePatient(Long patientId) {
        if (!patientRepository.existsById(patientId)) {
            throw new ResourceNotFoundException("Patient not found: " + patientId);
        }
    }

    /**
     * Loads a document and enforces that it belongs to the given patient. A mismatch returns
     * the same 404 as a missing document so callers cannot probe for documents that belong
     * to other patients (tenant isolation).
     */
    private DocumentEntity requireOwnedDocument(Long patientId, Long documentId) {
        DocumentEntity doc = documentRepository.findById(documentId)
                .orElseThrow(() -> new ResourceNotFoundException("Document not found: " + documentId));
        if (!doc.getPatientId().equals(patientId)) {
            throw new ResourceNotFoundException("Document not found: " + documentId);
        }
        return doc;
    }
}
