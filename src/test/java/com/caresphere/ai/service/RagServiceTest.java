package com.caresphere.ai.service;

import com.caresphere.ai.ChatGateway;
import com.caresphere.ai.dto.AnswerResponse;
import com.caresphere.ai.dto.QuestionRequest;
import com.caresphere.document.domain.DocumentEntity;
import com.caresphere.document.extract.ExtractedPdf;
import com.caresphere.document.extract.PdfTextExtractor;
import com.caresphere.document.repository.DocumentRepository;
import com.caresphere.document.storage.DocumentStorage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RagServiceTest {

    @Mock private VectorStore vectorStore;
    @Mock private DocumentRepository documentRepository;
    @Mock private DocumentStorage storage;
    @Mock private PdfTextExtractor extractor;
    @Mock private ChatGateway chatGateway;
    @Mock private com.caresphere.common.audit.AuditService auditService;

    private RagService ragService;

    @BeforeEach
    void setUp() {
        ragService = new RagService(vectorStore, documentRepository, storage, extractor, chatGateway, auditService);
    }

    private Document doc(String text, String patientId, int page) {
        return new Document(text, Map.of(
                "documentId", "1", "patientId", patientId, "filename", "report.pdf", "pageNumber", String.valueOf(page)));
    }

    @Test
    void answerReturnsGroundedAnswerWithCitations() {
        when(vectorStore.similaritySearch(any(SearchRequest.class)))
                .thenReturn(List.of(doc("The patient has hypertension managed with lisinopril.", "2", 1)));
        when(chatGateway.chat(any(), any())).thenReturn("The patient has hypertension, managed with lisinopril.");

        AnswerResponse response = ragService.answer(new QuestionRequest("What conditions does the patient have?", null));

        assertThat(response.evidenceSufficient()).isTrue();
        assertThat(response.answer()).contains("hypertension");
        assertThat(response.citations()).hasSize(1);
        assertThat(response.citations().get(0).pageNumber()).isEqualTo(1);
        assertThat(response.citations().get(0).filename()).isEqualTo("report.pdf");
    }

    @Test
    void answerReturnsInsufficientWhenNoRelevantPassages() {
        when(vectorStore.similaritySearch(any(SearchRequest.class))).thenReturn(List.of());

        AnswerResponse response = ragService.answer(new QuestionRequest("Anything?", null));

        assertThat(response.evidenceSufficient()).isFalse();
        assertThat(response.citations()).isEmpty();
    }

    @Test
    void answerReturnsInsufficientWhenModelCannotAnswer() {
        when(vectorStore.similaritySearch(any(SearchRequest.class)))
                .thenReturn(List.of(doc("Unrelated text.", "2", 1)));
        when(chatGateway.chat(any(), any())).thenReturn("INSUFFICIENT EVIDENCE");

        AnswerResponse response = ragService.answer(new QuestionRequest("What is the diagnosis?", null));

        assertThat(response.evidenceSufficient()).isFalse();
        assertThat(response.citations()).hasSize(1);
    }

    @Test
    void answerIsScopedByPatientId() {
        when(vectorStore.similaritySearch(any(SearchRequest.class)))
                .thenReturn(List.of(doc("belongs to patient 1", "1", 2), doc("belongs to patient 9", "9", 5)));
        when(chatGateway.chat(any(), any())).thenReturn("ok");

        AnswerResponse response = ragService.answer(new QuestionRequest("question", 1L));

        assertThat(response.citations()).hasSize(1);
        assertThat(response.citations().get(0).pageNumber()).isEqualTo(2);
    }

    @Test
    void indexDocumentChunksAndStoresEmbeddings() throws Exception {
        DocumentEntity entity = new DocumentEntity();
        entity.setId(7L);
        entity.setPatientId(3L);
        entity.setStorageKey("3/x.pdf");
        entity.setFilename("x.pdf");
        when(documentRepository.findById(7L)).thenReturn(Optional.of(entity));
        when(storage.load("3/x.pdf")).thenReturn(new byte[]{});
        when(extractor.extract(any(byte[].class))).thenReturn(new ExtractedPdf(List.of("alpha beta", "gamma delta"), 2));
        // vectorStore.add returns void; the default Mockito behavior is a no-op, so no stub needed.

        ragService.indexDocument(7L);

        org.mockito.Mockito.verify(vectorStore).add(any());
        assertThat(entity.getPageCount()).isEqualTo(2);
    }
}
