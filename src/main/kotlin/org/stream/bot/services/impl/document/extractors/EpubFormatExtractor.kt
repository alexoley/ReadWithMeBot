package org.stream.bot.services.impl.document.extractors

import org.apache.tika.Tika
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.stream.bot.services.IDocumentFormatExtractor
import java.io.File

@Service
class EpubFormatExtractor(@Value("\${bot.message.max-size}") val pageSize: String): IDocumentFormatExtractor {

    private val logger = LoggerFactory.getLogger(javaClass)

    private val format: String = "application/epub+zip"

    override fun getDocumentMimeType(): String {
        return format
    }

    override fun extractTextFromDocumentPage(file: File, page: Int): String {
        val text = Tika().parseToString(file)
        val size = pageSize.toInt()
        //pages are 1-based (see DocumentPosition)
        val pageBegin = size * (page - 1)
        if (page < 1 || pageBegin >= text.length)
            return ""
        val pageEnd = (size * page).coerceAtMost(text.length)
        return text.substring(pageBegin, pageEnd)
    }

    override fun numberOfPages(file: File): Int {
        val textLength = Tika().parseToString(file).length
        return if (textLength % pageSize.toInt() > 0)
            (textLength / pageSize.toInt()) + 1
        else
            textLength / pageSize.toInt()
    }
}