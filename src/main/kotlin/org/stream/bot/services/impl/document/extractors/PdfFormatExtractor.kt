package org.stream.bot.services.impl.document.extractors

import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.text.PDFTextStripper
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.stream.bot.services.IDocumentFormatExtractor
import java.io.File

@Service
class PdfFormatExtractor: IDocumentFormatExtractor {

    private val logger = LoggerFactory.getLogger(javaClass)

    val format: String = "application/pdf"

    override fun getDocumentFormat(): String {
        return format;
    }

    override fun extractTextFromDocument(file: File, startPage: Int, endPage: Int): String {
        PDDocument.load(file).use { doc ->
            val stripper = PDFTextStripper()
            stripper.setStartPage(startPage)
            stripper.setEndPage(endPage)
            val text = stripper.getText(doc)
            return text
        }
    }

    override fun numberOfPages(file: File): Int {
        PDDocument.load(file).use { return it.numberOfPages }
    }
}