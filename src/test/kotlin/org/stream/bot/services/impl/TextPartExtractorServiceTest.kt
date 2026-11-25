package org.stream.bot.services.impl

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.stream.bot.TestFixtures
import org.stream.bot.entities.DocumentPosition
import org.stream.bot.entities.FileInfo
import org.stream.bot.exceptions.NoSuchDocumentFormatExtractor
import org.stream.bot.services.IDocumentFormatExtractor
import org.stream.bot.services.IDocumentPersistManager
import java.io.File

class TextPartExtractorServiceTest {

    private val mimeType = "text/test"
    private val file = File("fake-book")

    /** In-memory extractor: page N (1-based) is pages[N-1]. */
    private class FakeExtractor(private val mime: String, var pages: List<String>) : IDocumentFormatExtractor {
        override fun getDocumentMimeType() = mime
        override fun extractTextFromDocumentPage(file: File, page: Int) = pages.getOrElse(page - 1) { "" }
        override fun numberOfPages(file: File) = pages.size
    }

    private lateinit var extractor: FakeExtractor
    private lateinit var persistManager: IDocumentPersistManager
    private lateinit var service: TextPartExtractorService
    private lateinit var fileInfo: FileInfo

    @BeforeEach
    fun setUp() {
        extractor = FakeExtractor(mimeType, emptyList())
        persistManager = mock(IDocumentPersistManager::class.java)
        service = TextPartExtractorService(listOf(extractor))
        service.persistManager = persistManager
        service.messageSize = "100"
        fileInfo = TestFixtures.fileInfo(fileName = "book.txt", mimeType = mimeType)
        `when`(persistManager.downloadFromStorage(fileInfo)).thenReturn(file)
    }

    /** Header is "book.txt  " (10) + 7 + 2 * digits(pages); body limit is messageSize minus that. */
    private fun bodyLimit(numberOfPages: Int) = 100 - (10 + 7 + numberOfPages.toString().length * 2)

    private fun body(result: String) = result.substringAfter("\n\n")

    @Test
    fun `registers extractors by mime type`() {
        assertEquals(setOf(mimeType), service.documentFormatExtractors.keys)
    }

    @Test
    fun `throws for unsupported mime type`() {
        assertThrows(NoSuchDocumentFormatExtractor::class.java) {
            service.getNextTextPartAndPosition(fileInfo.copy(mimeType = "application/unknown"))
        }
    }

    @Test
    fun `single short page is sent whole and book is finished`() {
        extractor.pages = listOf("Short book. ")

        val result = service.getNextTextPartAndPosition(fileInfo)

        assertEquals("book.txt  page 1\n\nShort book. ", result)
        assertFalse(fileInfo.stillReading)
        assertEquals(DocumentPosition(1, 12), fileInfo.lastSentPage)
    }

    @Test
    fun `several short pages are merged into one message`() {
        extractor.pages = listOf("Page one. ", "Page two. ")

        val result = service.getNextTextPartAndPosition(fileInfo)

        assertTrue(result.startsWith("book.txt  pages 1-2\n\n"), result)
        assertEquals("Page one. Page two. ", body(result))
        assertFalse(fileInfo.stillReading)
        assertEquals(2, fileInfo.lastSentPage.page)
    }

    @Test
    fun `long page is cut at the last sentence end and fits message size`() {
        val sentence = "Sentence number. "  // 17 chars
        extractor.pages = listOf(sentence.repeat(20))

        val result = service.getNextTextPartAndPosition(fileInfo)

        assertTrue(result.length <= 100, "message is ${result.length} chars")
        assertTrue(body(result).endsWith(". "), body(result))
        assertEquals(0, body(result).length % sentence.length, "only whole sentences are sent")
        assertTrue(fileInfo.stillReading)
        assertEquals(DocumentPosition(1, body(result).length), fileInfo.lastSentPage)
    }

    @Test
    fun `consecutive calls read the whole document without gaps or repeats`() {
        val pages = listOf(
                "First page starts here. It has a few sentences! Does it? Yes. ",
                "Second page is here. ".repeat(6),
                "Third and last page. The end. ")
        extractor.pages = pages

        val collected = StringBuilder()
        var calls = 0
        while (fileInfo.stillReading && calls < 50) {
            val result = service.getNextTextPartAndPosition(fileInfo)
            assertTrue(result.length <= 100, "message is ${result.length} chars: $result")
            collected.append(body(result))
            calls++
        }

        assertFalse(fileInfo.stillReading)
        assertEquals(pages.joinToString(""), collected.toString())
    }

    @Test
    fun `text without punctuation is cut by size`() {
        extractor.pages = listOf("A".repeat(1000))

        val result = service.getNextTextPartAndPosition(fileInfo)

        assertEquals("A".repeat(bodyLimit(1)), body(result))
        assertTrue(result.length <= 100)
        assertEquals(DocumentPosition(1, bodyLimit(1)), fileInfo.lastSentPage)
        assertTrue(fileInfo.stillReading)
    }

    @Test
    fun `continues from saved letter position`() {
        extractor.pages = listOf("Already read. Not read yet. ")
        fileInfo.lastSentPage = DocumentPosition(1, "Already read. ".length)

        val result = service.getNextTextPartAndPosition(fileInfo)

        assertEquals("Not read yet. ", body(result))
        assertFalse(fileInfo.stillReading)
    }

    @Test
    fun `book is not marked finished when text exactly fills message but pages remain`() {
        val limit = bodyLimit(2)
        extractor.pages = listOf("B".repeat(limit - 2) + ". ", "Remaining page. ")

        service.getNextTextPartAndPosition(fileInfo)

        assertTrue(fileInfo.stillReading)
        val second = service.getNextTextPartAndPosition(fileInfo)
        assertEquals("Remaining page. ", body(second))
        assertFalse(fileInfo.stillReading)
    }
}
