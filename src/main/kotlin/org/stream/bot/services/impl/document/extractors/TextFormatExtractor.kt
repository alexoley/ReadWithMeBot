package org.stream.bot.services.impl.document.extractors

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.stream.bot.services.IDocumentFormatExtractor
import java.io.File

@Service
class TextFormatExtractor: IDocumentFormatExtractor  {

    private val logger = LoggerFactory.getLogger(javaClass)

    val format: String = "text/plain"

    override fun getDocumentMimeType(): String {
        return format
    }

    override fun extractTextFromDocumentPage(file: File, page: Int): String {
        val listOfLines = file.readLines()
        if (page<=listOfLines.size)
            return listOfLines.get(page)
        else
            return ""
    }

    override fun numberOfPages(file: File): Int {
        return file.readLines().size
    }
}