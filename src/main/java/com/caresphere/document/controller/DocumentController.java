package com.caresphere.document.controller;

import com.caresphere.document.dto.DocumentResponse;
import com.caresphere.document.dto.DocumentTextResponse;
import com.caresphere.document.service.DocumentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "Documents", description = "Secure upload and PDF text extraction")
@RestController
@RequestMapping("/api/patients/{patientId}/documents")
public class DocumentController {

    private final DocumentService documentService;

    public DocumentController(DocumentService documentService) {
        this.documentService = documentService;
    }

    @Operation(summary = "Upload a PDF document and extract its text")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<DocumentResponse> upload(@PathVariable Long patientId,
                                                  @RequestParam("file") MultipartFile file) {
        return ResponseEntity.status(HttpStatus.CREATED).body(documentService.upload(patientId, file));
    }

    @Operation(summary = "List documents for a patient (paginated)")
    @GetMapping
    public Page<DocumentResponse> list(@PathVariable Long patientId, Pageable pageable) {
        return documentService.list(patientId, pageable);
    }

    @Operation(summary = "Get document metadata")
    @GetMapping("/{documentId}")
    public DocumentResponse get(@PathVariable Long patientId, @PathVariable Long documentId) {
        return documentService.get(patientId, documentId);
    }

    @Operation(summary = "Get the extracted text of a document")
    @GetMapping("/{documentId}/text")
    public DocumentTextResponse getText(@PathVariable Long patientId, @PathVariable Long documentId) {
        return documentService.getText(patientId, documentId);
    }
}
