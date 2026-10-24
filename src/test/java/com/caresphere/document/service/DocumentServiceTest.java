package com.caresphere.document.service;

import com.caresphere.auth.security.AuthenticatedUser;
import com.caresphere.common.exception.DocumentProcessingException;
import com.caresphere.common.exception.ResourceNotFoundException;
import com.caresphere.document.domain.DocumentEntity;
import com.caresphere.document.dto.DocumentResponse;
import com.caresphere.document.extract.ExtractedPdf;
import com.caresphere.document.extract.PdfTextExtractor;
import com.caresphere.document.repository.DocumentRepository;
import com.caresphere.document.storage.DocumentStorage;
import com.caresphere.patient.repository.PatientRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DocumentServiceTest {

    @Mock private DocumentRepository documentRepository;
    @Mock private PatientRepository patientRepository;
    @Mock private DocumentStorage storage;
    @Mock private PdfTextExtractor extractor;
    @Mock private AuthenticatedUser authenticatedUser;
    @Mock private com.caresphere.common.audit.AuditService auditService;

    private DocumentService service;

    @BeforeEach
    void setUp() {
        service = new DocumentService(documentRepository, patientRepository, storage, extractor, authenticatedUser, auditService, 10);
    }

    private MockMultipartFile pdf(String content) {
        return new MockMultipartFile("file", "report.pdf", "application/pdf", content.getBytes());
    }

    @Test
    void uploadExtractsTextAndMarksDocumentExtracted() throws Exception {
        when(patientRepository.existsById(1L)).thenReturn(true);
        when(authenticatedUser.getUsername()).thenReturn("clinician@caresphere.dev");
        when(storage.store(eq(1L), any())).thenReturn("1/abc-report.pdf");
        when(extractor.extract(any())).thenReturn(new ExtractedPdf(List.of("page one", "page two"), 2));
        when(documentRepository.save(any(DocumentEntity.class))).thenAnswer(inv -> {
            DocumentEntity d = inv.getArgument(0);
            d.setId(1L);
            return d;
        });

        DocumentResponse response = service.upload(1L, pdf("PDFBYTES"));

        assertThat(response.status()).isEqualTo("EXTRACTED");
        assertThat(response.pageCount()).isEqualTo(2);
        assertThat(response.uploadedBy()).isEqualTo("clinician@caresphere.dev");
    }

    @Test
    void uploadRejectsNonPdfContent() {
        when(patientRepository.existsById(1L)).thenReturn(true);
        MockMultipartFile txt = new MockMultipartFile("file", "notes.txt", "text/plain", "hello".getBytes());
        assertThatThrownBy(() -> service.upload(1L, txt)).isInstanceOf(DocumentProcessingException.class);
    }

    @Test
    void uploadRejectsUnknownPatient() {
        when(patientRepository.existsById(99L)).thenReturn(false);
        assertThatThrownBy(() -> service.upload(99L, pdf("PDFBYTES"))).isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void getRejectsDocumentBelongingToAnotherPatient() {
        // tenant isolation: a document owned by patient 2 cannot be read via patient 1's path
        DocumentEntity ownedByAnother = new DocumentEntity();
        ownedByAnother.setId(1L);
        ownedByAnother.setPatientId(2L);
        when(documentRepository.findById(1L)).thenReturn(Optional.of(ownedByAnother));

        assertThatThrownBy(() -> service.get(1L, 1L)).isInstanceOf(ResourceNotFoundException.class);
    }
}
