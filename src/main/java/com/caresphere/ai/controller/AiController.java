package com.caresphere.ai.controller;

import com.caresphere.ai.dto.AnswerResponse;
import com.caresphere.ai.dto.QuestionRequest;
import com.caresphere.ai.service.RagService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "AI", description = "RAG document search and grounded answers")
@RestController
public class AiController {

    private final RagService ragService;

    public AiController(RagService ragService) {
        this.ragService = ragService;
    }

    @Operation(summary = "Ask a question answered from indexed documents (RAG)")
    @PostMapping("/api/ai/answer")
    public AnswerResponse answer(@Valid @RequestBody QuestionRequest request) {
        return ragService.answer(request);
    }

    @Operation(summary = "Index a document's text into the vector store for RAG")
    @PostMapping("/api/patients/{patientId}/documents/{documentId}/index")
    public ResponseEntity<Void> index(@PathVariable Long patientId, @PathVariable Long documentId) {
        ragService.indexDocument(documentId);
        return ResponseEntity.accepted().build();
    }
}
