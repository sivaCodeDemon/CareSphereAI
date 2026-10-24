package com.caresphere.ai.service;

import com.caresphere.ai.ChatGateway;
import com.caresphere.ai.TextChunker;
import com.caresphere.ai.dto.AnswerResponse;
import com.caresphere.ai.dto.Citation;
import com.caresphere.ai.dto.QuestionRequest;
import com.caresphere.common.exception.ResourceNotFoundException;
import com.caresphere.document.domain.DocumentEntity;
import com.caresphere.document.extract.ExtractedPdf;
import com.caresphere.document.extract.PdfTextExtractor;
import com.caresphere.document.repository.DocumentRepository;
import com.caresphere.document.storage.DocumentStorage;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Retrieval-Augmented Generation. Indexes a document's text (page-aware chunks with metadata)
 * into the pgvector store, and answers questions grounded only in retrieved passages —
 * returning an explicit insufficient-evidence response when no relevant passages are found.
 */
@Service
@Transactional
public class RagService {

    private static final int CHUNK_SIZE = 1000;
    private static final int CHUNK_OVERLAP = 200;
    private static final int TOP_K = 6;
    private static final int MAX_CITATIONS = 4;
    private static final String INSUFFICIENT_MARKER = "INSUFFICIENT EVIDENCE";

    private static final String SYSTEM_PROMPT = """
            You are a careful medical document assistant for an educational platform.
            Answer the user's question using ONLY the provided context passages.
            Do not invent facts or rely on outside knowledge.
            If the context does not contain enough information to answer, respond exactly:
            INSUFFICIENT EVIDENCE
            Keep the answer concise and cite nothing yourself; citations are added separately.
            """;

    private final VectorStore vectorStore;
    private final DocumentRepository documentRepository;
    private final DocumentStorage storage;
    private final PdfTextExtractor extractor;
    private final ChatGateway chatGateway;
    private final com.caresphere.common.audit.AuditService auditService;

    public RagService(VectorStore vectorStore, DocumentRepository documentRepository, DocumentStorage storage,
                      PdfTextExtractor extractor, ChatGateway chatGateway,
                      com.caresphere.common.audit.AuditService auditService) {
        this.vectorStore = vectorStore;
        this.documentRepository = documentRepository;
        this.storage = storage;
        this.extractor = extractor;
        this.chatGateway = chatGateway;
        this.auditService = auditService;
    }

    public void indexDocument(Long documentId) {
        DocumentEntity doc = documentRepository.findById(documentId)
                .orElseThrow(() -> new ResourceNotFoundException("Document not found: " + documentId));

        byte[] bytes;
        try {
            bytes = storage.load(doc.getStorageKey());
        } catch (IOException e) {
            throw new IllegalStateException("Failed to read stored document " + documentId, e);
        }

        ExtractedPdf extracted = extractor.extract(bytes);
        List<Document> chunks = new ArrayList<>();
        List<String> pages = extracted.pages();
        for (int p = 0; p < pages.size(); p++) {
            String pageText = pages.get(p);
            for (String chunk : TextChunker.chunk(pageText, CHUNK_SIZE, CHUNK_OVERLAP)) {
                chunks.add(new Document(chunk, Map.of(
                        "documentId", String.valueOf(doc.getId()),
                        "patientId", String.valueOf(doc.getPatientId()),
                        "filename", doc.getFilename(),
                        "pageNumber", String.valueOf(p + 1))));
            }
        }
        if (!chunks.isEmpty()) {
            vectorStore.add(chunks);
        }
        doc.setPageCount(extracted.pageCount());
        documentRepository.save(doc);
    }

    public AnswerResponse answer(QuestionRequest request) {
        SearchRequest search = SearchRequest.builder().query(request.question()).topK(TOP_K).build();
        List<Document> hits = vectorStore.similaritySearch(search);
        if (hits == null) {
            hits = List.of();
        }

        List<Document> scoped = (request.patientId() == null)
                ? hits
                : hits.stream().filter(d -> matchesPatient(d, request.patientId())).toList();

        if (scoped.isEmpty()) {
            return AnswerResponse.insufficient(List.of());
        }

        List<Citation> citations = scoped.stream().limit(MAX_CITATIONS).map(this::toCitation).toList();
        String context = scoped.stream().map(this::formatForContext).collect(Collectors.joining("\n\n"));
        String userPrompt = "Context passages:\n" + context
                + "\n\nQuestion: " + request.question() + "\n\nAnswer:";

        String answer = chatGateway.chat(SYSTEM_PROMPT, userPrompt);
        AnswerResponse response = (answer == null || answer.contains(INSUFFICIENT_MARKER))
                ? AnswerResponse.insufficient(citations)
                : new AnswerResponse(answer.strip(), true, citations);
        auditService.record("ANSWER_QUESTION", "AI", null, request.patientId(),
                response.evidenceSufficient() ? "SUCCESS" : "INSUFFICIENT");
        return response;
    }

    private boolean matchesPatient(Document d, Long patientId) {
        Object value = d.getMetadata().get("patientId");
        return value != null && String.valueOf(patientId).equals(String.valueOf(value));
    }

    private Citation toCitation(Document d) {
        Map<String, Object> meta = d.getMetadata();
        Long documentId = parseLong(meta.get("documentId"));
        int pageNumber = parseInt(meta.get("pageNumber"), 1);
        String filename = String.valueOf(meta.getOrDefault("filename", "unknown"));
        String snippet = truncate(d.getText(), 240);
        return new Citation(documentId, filename, pageNumber, snippet);
    }

    private String formatForContext(Document d) {
        int page = parseInt(d.getMetadata().get("pageNumber"), 1);
        return "[page " + page + "] " + d.getText();
    }

    private static Long parseLong(Object o) {
        try {
            return (o == null) ? null : Long.valueOf(String.valueOf(o));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static int parseInt(Object o, int fallback) {
        try {
            return (o == null) ? fallback : Integer.parseInt(String.valueOf(o));
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    private static String truncate(String text, int max) {
        if (text == null) {
            return "";
        }
        return text.length() <= max ? text : text.substring(0, max) + "…";
    }
}
