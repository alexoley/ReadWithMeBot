package org.stream.bot.services.impl.document.extractors

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.stream.bot.services.IDocumentFormatExtractor
import java.io.File

@Service
class TextFormatExtractor : IDocumentFormatExtractor {

    private val logger = LoggerFactory.getLogger(javaClass)

    val format: String = "text/plain"

    override fun getDocumentMimeType(): String {
        return format
    }

    override fun extractTextFromDocumentPage(file: File, page: Int): String {
        //pages are 1-based (see DocumentPosition), one line is one page
        val listOfLines = file.readLines()
        return if (page in 1..listOfLines.size)
            listOfLines[page - 1] + "\n"
        else
            ""
    }

    override fun numberOfPages(file: File): Int {
        return file.readLines().size
    }
}