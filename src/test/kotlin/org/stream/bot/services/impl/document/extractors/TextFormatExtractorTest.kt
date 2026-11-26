package org.stream.bot.services.impl.document.extractors

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

class TextFormatExtractorTest {

    private val extractor = TextFormatExtractor()

    private fun bookFile(dir: File): File {
        val file = File(dir, "book.txt")
        file.writeText("First line.\nSecond line.\nLast line.")
        return file
    }

    @Test
    fun `reports telegram mime type`() {
        assertEquals("text/plain", extractor.getDocumentMimeType())
    }

    @Test
    fun `counts one page per line`(@TempDir dir: File) {
        assertEquals(3, extractor.numberOfPages(bookFile(dir)))
    }

    @Test
    fun `first page is the first line`(@TempDir dir: File) {
        assertEquals("First line.\n", extractor.extractTextFromDocumentPage(bookFile(dir), 1))
    }

    @Test
    fun `last page is the last line`(@TempDir dir: File) {
        assertEquals("Last line.\n", extractor.extractTextFromDocumentPage(bookFile(dir), 3))
    }

    @Test
    fun `pages outside the file are empty`(@TempDir dir: File) {
        val file = bookFile(dir)
        assertEquals("", extractor.extractTextFromDocumentPage(file, 0))
        assertEquals("", extractor.extractTextFromDocumentPage(file, 4))
    }
}
