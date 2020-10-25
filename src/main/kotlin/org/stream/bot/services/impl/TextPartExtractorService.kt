package org.stream.bot.services.impl

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.stream.bot.entities.DocumentPosition
import org.stream.bot.entities.FileInfo
import org.stream.bot.exceptions.NoSuchDocumentFormatExtractor
import org.stream.bot.services.IDocumentFormatExtractor
import org.stream.bot.services.IDocumentPersistManager
import java.util.function.Predicate

@Service
class TextPartExtractorService {

    private val logger = LoggerFactory.getLogger(javaClass)

    @Autowired
    constructor(listOfDocumentFormatExtractors: List<IDocumentFormatExtractor>){
        documentFormatExtractors = listOfDocumentFormatExtractors.asSequence().map { it.getDocumentMimeType() to it }.toMap()
    }

    //map of mime types opposite document format extractor implementations
    var documentFormatExtractors : Map<String, IDocumentFormatExtractor>

    @Autowired
    lateinit var persistManager: IDocumentPersistManager

    @Value("\${bot.message.size}")
    lateinit var messageSize: String


    fun getNextTextPartAndPosition(fileInfo: FileInfo): String {
        val documentFormatExtractor = documentFormatExtractors[fileInfo.mimeType] ?: throw NoSuchDocumentFormatExtractor()
        val file = persistManager.downloadFromStorage(fileInfo)
        val numberOfPages = documentFormatExtractor.numberOfPages(file)
        var position = fileInfo.lastSentPage.letterPosition
        var page = fileInfo.lastSentPage.page
        var nextPages = mutableListOf<Pair<Int, String>>()
        var header = "${fileInfo.fileName}  "
        //header example: mylovelybook.pdf pages.1765-1768
        val messageSizeMinusHeader = (messageSize.toInt().minus(header.length.plus(7 + (numberOfPages.toString().length) * 2)))

        //add new pages to list
        nextPages.add(Pair(page, documentFormatExtractor.extractTextFromDocumentPage(file, page).substring(position)))
        while (nextPages.asSequence()
                        .map { it.second.length }
                        .reduce { e1, e2 -> e1 + e2 } < messageSizeMinusHeader && page < numberOfPages) {
            page++
            nextPages.add(Pair(page, documentFormatExtractor.extractTextFromDocumentPage(file, page)))
        }


        if (nextPages.asSequence().map { it.second.length }.reduce { e1, e2 -> e1 + e2 } > messageSizeMinusHeader) {
            //fit last string
            val lastPageSubstringEndIndex = nextPages.last().second.length -
                    (nextPages.asSequence().map { it.second.length }.reduce { e1, e2 -> e1 + e2 } - messageSizeMinusHeader)
            nextPages[nextPages.size - 1] = Pair(nextPages.last().first,
                    nextPages.last().second.substring(0, lastPageSubstringEndIndex))

            val nextPagesCopy = nextPages

            //fit to the first punctuation
            nextPages = nextPages.trimToPunctuation(messageSizeMinusHeader)

            //check if list empty, it means that algorithm cannot find stop symbol. Then return all symbols
            nextPages = if (nextPages.isNullOrEmpty() && nextPagesCopy.isNotEmpty()) nextPagesCopy else nextPages
        }
        else{
            fileInfo.stillReading=false
        }

        //check if we read same page, when position will be incremented by text length
        //and form a header
        if (nextPages.last().first == fileInfo.lastSentPage.page) {
            position = position + nextPages.last().second.length
            header = "${header}page ${fileInfo.lastSentPage.page}\n\n"
        } else {
            position = nextPages.last().second.length
            header = "${header}pages ${fileInfo.lastSentPage.page}-${nextPages.last().first}\n\n"
        }

        fileInfo.lastSentPage=DocumentPosition(nextPages.last().first, position)
        return header + nextPages.asSequence().map { e -> e.second }.reduce { e1, e2 -> e1 + e2 }
    }

    fun MutableList<Pair<Int, String>>.trimToPunctuation(size: Int): MutableList<Pair<Int, String>> {
        if (this.isEmpty()) {
            return this
        } else {
            val newString = StringBuilder(this.last().second)
            while (newString.isNotEmpty().and(checkStopSymbols.test(newString)))
                newString.deleteCharAt(newString.length - 1)
            if (newString.isNotEmpty()) {
                this[this.size - 1] = Pair(this.last().first, newString.toString())
                return this
            }
            return this.subList(0, this.size - 1).trimToPunctuation(size)
        }
    }

    val checkStopSymbols = Predicate<StringBuilder> {
        (it.endsWith(". ")
                .or(it.endsWith("... "))
                .or(it.endsWith("! "))
                .or(it.endsWith("? "))
                .or(it.endsWith(".\t"))
                .or(it.endsWith("...\t"))
                .or(it.endsWith("!\t"))
                .or(it.endsWith("?\t"))
                .or(it.endsWith(".\n"))
                .or(it.endsWith("...\n"))
                .or(it.endsWith("!\n"))
                .or(it.endsWith("?\n"))).not()
    }
}