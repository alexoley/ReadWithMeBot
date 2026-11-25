package org.stream.bot.services.impl.document.extractors

import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.pdmodel.PDPage
import org.apache.pdfbox.pdmodel.PDPageContentStream
import org.apache.pdfbox.pdmodel.font.PDType1Font
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

class PdfFormatExtractorTest {

    private val extractor = PdfFormatExtractor()

    private fun pdf(dir: File, vararg pageTexts: String): File {
        val file = File(dir, "book.pdf")
        PDDocument().use { doc ->
            pageTexts.forEach { text ->
                val page = PDPage()
                doc.addPage(page)
                PDPageContentStream(doc, page).use { stream ->
                    stream.beginText()
                    stream.setFont(PDType1Font.HELVETICA, 12f)
                    stream.newLineAtOffset(50f, 700f)
                    stream.showText(text)
                    stream.endText()
                }
            }
            doc.save(file)
        }
        return file
    }

    @Test
    fun `reports telegram mime type`() {
        assertEquals("application/pdf", extractor.getDocumentMimeType())
    }

    @Test
    fun `counts pages`(@TempDir dir: File) {
        assertEquals(2, extractor.numberOfPages(pdf(dir, "Alpha page.", "Beta page.")))
    }

    @Test
    fun `extracts text of the requested page only`(@TempDir dir: File) {
        val file = pdf(dir, "Alpha page.", "Beta page.")

        val first = extractor.extractTextFromDocumentPage(file, 1)
        val second = extractor.extractTextFromDocumentPage(file, 2)

        assertTrue(first.contains("Alpha page."), first)
        assertFalse(first.contains("Beta"), first)
        assertTrue(second.contains("Beta page."), second)
        assertFalse(second.contains("Alpha"), second)
    }
}
