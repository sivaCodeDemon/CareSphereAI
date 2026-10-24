package com.caresphere.ai.dto;

/**
 * A source citation backing an AI answer.
 *
 * @param documentId id of the source document
 * @param filename   name of the source file
 * @param pageNumber 1-based page number in the source PDF
 * @param snippet    the retrieved passage used as evidence
 */
public record Citation(Long documentId, String filename, int pageNumber, String snippet) {
}
