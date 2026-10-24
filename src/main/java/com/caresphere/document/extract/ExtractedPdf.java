package com.caresphere.document.extract;

import java.util.List;

/**
 * Result of PDF text extraction.
 *
 * @param pages     one text entry per page (1-indexed in the source PDF)
 * @param pageCount number of pages in the source PDF
 */
public record ExtractedPdf(List<String> pages, int pageCount) {

    /** Full text with a blank-line separator between pages. */
    public String fullText() {
        return String.join("\n\n", pages());
    }
}
