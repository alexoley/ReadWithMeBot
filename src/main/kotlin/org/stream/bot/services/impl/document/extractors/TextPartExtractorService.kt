package org.stream.bot.services.impl.document.extractors

import org.apache.pdfbox.pdmodel.PDDocument
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service
import org.stream.bot.entities.DocumentPosition
import org.stream.bot.entities.FileInfo
import org.stream.bot.services.IDocumentFormatExtractor
import org.stream.bot.services.IDocumentPersistManager
import java.io.File

@Service
class TextPartExtractorService{

    private val logger = LoggerFactory.getLogger(javaClass)

    @Autowired
    lateinit var pdfFormatExtractor: IDocumentFormatExtractor

    @Autowired
    lateinit var persistManager: IDocumentPersistManager


    fun getNextTextPartAndPosition(fileInfo: FileInfo): Pair<String, DocumentPosition>{
        val file = persistManager.uploadFromStorage(fileInfo)
        var page = fileInfo.lastSentPage.page
        var numberOfPages=0
        PDDocument.load(file).use { numberOfPages=it.numberOfPages }
        var position =fileInfo.lastSentPage.letterPosition
        var sb = ""
        sb+=pdfFormatExtractor.extractTextFromDocument(file, page, page+1).substring(position)
        position+=sb.length

        //TODO: add list for pages? or hashmap?
        while (sb.length<4096 && page<numberOfPages){
            page++
            val newPageText=pdfFormatExtractor.extractTextFromDocument(file, page, page+1)
            position=newPageText.length
            sb+=newPageText
        }

        while (((sb.endsWith(". ")
                        .or(sb.endsWith("... ")
                                .or(sb.endsWith("! "))
                                .or(sb.endsWith("? ")))).not()
                || (sb.endsWith(".\t")
                        .or(sb.endsWith("...\t")
                                .or(sb.endsWith("!\t"))
                                .or(sb.endsWith("?\t")))).not()).and(sb.length>0) ||
                (sb.length>4096)){
            sb=sb.dropLast(1)
            if (position==0){
                position=pdfFormatExtractor.extractTextFromDocument(file, page-1, page).length
            }
            position--
        }

        return Pair(sb, DocumentPosition(page,position))
    }

    fun tempGetNextTextPartAndPosition(localFile: File, initPage: Int, initPosition: Int): Pair<String, DocumentPosition>{
        var page = initPage
        var numberOfPages=0
        PDDocument.load(localFile).use { numberOfPages=it.numberOfPages }
        logger.info("Number of pages: $numberOfPages")
        var position = initPosition
        var sb=""
        sb+=(pdfFormatExtractor.extractTextFromDocument(localFile, page, page+1).substring(position))
        position+=(sb.length)
        while (sb.length<4096 && page<numberOfPages){
            page++
            val newPageText=pdfFormatExtractor.extractTextFromDocument(localFile, page, page+1)
            position=newPageText.length
            sb+=(newPageText)
        }

        while (sb.endsWith(". ")
                        .or(sb.endsWith("... ")
                                .or(sb.endsWith("! ")).or(sb.endsWith("? "))).not()
                        .or(sb.length>4096)
                        .and(sb.length>0)){
            sb=sb.dropLast(1)
            position--
        }
        return Pair(sb, DocumentPosition(page,position))
    }
}