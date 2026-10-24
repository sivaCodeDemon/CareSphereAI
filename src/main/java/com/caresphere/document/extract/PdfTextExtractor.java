package com.caresphere.document.extract;

import com.caresphere.common.exception.DocumentProcessingException;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Extracts plain text from a PDF, page by page, using Apache PDFBox. Per-page output is
 * essential for grounding RAG citations in the original page number.
 */
@Component
public class PdfTextExtractor {

    /**
     * @param bytes raw PDF bytes
     * @return the extracted text split per page plus the page count
     */
    public ExtractedPdf extract(byte[] bytes) {
        try (PDDocument pdf = Loader.loadPDF(bytes)) {
            PDFTextStripper stripper = new PDFTextStripper();
            int pageCount = pdf.getNumberOfPages();
            List<String> pages = new ArrayList<>(pageCount);
            for (int i = 1; i <= pageCount; i++) {
                stripper.setStartPage(i);
                stripper.setEndPage(i);
                pages.add(stripper.getText(pdf).strip());
            }
            return new ExtractedPdf(pages, pageCount);
        } catch (IOException e) {
            throw new DocumentProcessingException("Failed to read PDF content");
        }
    }
}
