package com.caresphere.document.extract;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;

import static org.assertj.core.api.Assertions.assertThat;

class PdfTextExtractorTest {

    private final PdfTextExtractor extractor = new PdfTextExtractor();

    @Test
    void extractsTextPerPage() throws Exception {
        byte[] pdf = buildTwoPagePdf();

        ExtractedPdf result = extractor.extract(pdf);

        assertThat(result.pageCount()).isEqualTo(2);
        assertThat(result.pages().get(0)).contains("page one");
        assertThat(result.pages().get(1)).contains("page two");
        assertThat(result.fullText()).contains("page one").contains("page two");
    }

    private byte[] buildTwoPagePdf() throws Exception {
        try (PDDocument doc = new PDDocument();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            doc.addPage(writeText(new PDPage(), "CareSphere page one", doc));
            doc.addPage(writeText(new PDPage(), "CareSphere page two", doc));
            doc.save(out);
            return out.toByteArray();
        }
    }

    private PDPage writeText(PDPage page, String text, PDDocument doc) throws Exception {
        try (PDPageContentStream cs = new PDPageContentStream(doc, page)) {
            cs.beginText();
            cs.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 12);
            cs.newLineAtOffset(20, 700);
            cs.showText(text);
            cs.endText();
        }
        return page;
    }
}
