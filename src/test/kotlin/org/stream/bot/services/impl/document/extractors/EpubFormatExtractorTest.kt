package org.stream.bot.services.impl.document.extractors

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.util.zip.CRC32
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

class EpubFormatExtractorTest {

    private val pageSize = 10
    private val extractor = EpubFormatExtractor(pageSize.toString())

    /** Builds a minimal valid EPUB 2 file with one chapter. */
    private fun epub(dir: File, text: String): File {
        val file = File(dir, "book.epub")
        ZipOutputStream(file.outputStream()).use { zip ->
            val mimetype = "application/epub+zip".toByteArray()
            val stored = ZipEntry("mimetype")
            stored.method = ZipEntry.STORED
            stored.size = mimetype.size.toLong()
            stored.crc = CRC32().apply { update(mimetype) }.value
            zip.putNextEntry(stored)
            zip.write(mimetype)
            zip.closeEntry()

            fun add(name: String, content: String) {
                zip.putNextEntry(ZipEntry(name))
                zip.write(content.toByteArray())
                zip.closeEntry()
            }
            add("META-INF/container.xml", """<?xml version="1.0"?>
<container version="1.0" xmlns="urn:oasis:names:tc:opendocument:xmlns:container">
  <rootfiles><rootfile full-path="content.opf" media-type="application/oebps-package+xml"/></rootfiles>
</container>""")
            add("content.opf", """<?xml version="1.0" encoding="UTF-8"?>
<package xmlns="http://www.idpf.org/2007/opf" version="2.0" unique-identifier="id">
  <metadata xmlns:dc="http://purl.org/dc/elements/1.1/"><dc:title>Test</dc:title><dc:identifier id="id">1</dc:identifier></metadata>
  <manifest><item id="ch1" href="chapter1.xhtml" media-type="application/xhtml+xml"/></manifest>
  <spine><itemref idref="ch1"/></spine>
</package>""")
            add("chapter1.xhtml", """<?xml version="1.0" encoding="UTF-8"?>
<html xmlns="http://www.w3.org/1999/xhtml"><head><title>c</title></head><body><p>$text</p></body></html>""")
        }
        return file
    }

    private fun allPages(file: File) =
            (1..extractor.numberOfPages(file)).joinToString("") { extractor.extractTextFromDocumentPage(file, it) }

    @Test
    fun `reports telegram mime type`() {
        assertEquals("application/epub+zip", extractor.getDocumentMimeType())
    }

    @Test
    fun `book shorter than one page is returned without errors`(@TempDir dir: File) {
        val file = epub(dir, "Hi.")

        assertEquals(1, extractor.numberOfPages(file))
        assertTrue(extractor.extractTextFromDocumentPage(file, 1).contains("Hi."))
    }

    @Test
    fun `pages split the text without overlap or loss`(@TempDir dir: File) {
        val text = "Once upon a time there was a small test book."
        val file = epub(dir, text)

        val pages = extractor.numberOfPages(file)
        assertTrue(pages > 1)
        (1 until pages).forEach { assertEquals(pageSize, extractor.extractTextFromDocumentPage(file, it).length) }
        assertTrue(allPages(file).contains(text), allPages(file))
    }

    @Test
    fun `page after the end is empty`(@TempDir dir: File) {
        val file = epub(dir, "Hi.")
        assertEquals("", extractor.extractTextFromDocumentPage(file, extractor.numberOfPages(file) + 1))
    }
}
