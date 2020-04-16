package org.stream.bot.services.impl

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


    override fun extractTextFromDocument(file: File): String {
        PDDocument.load(file).use { doc ->
            val stripper = PDFTextStripper()
            stripper.setStartPage(1)
            stripper.setEndPage(2)
            return stripper.getText(doc)
        }
    }

}