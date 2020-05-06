package org.stream.bot.services.impl.document.extractors

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.stream.bot.entities.DocumentPosition
import org.stream.bot.entities.FileInfo
import org.stream.bot.services.IDocumentFormatExtractor
import org.stream.bot.services.IDocumentPersistManager
import java.util.function.Predicate

@Service
class TextPartExtractorService {

    private val logger = LoggerFactory.getLogger(javaClass)

    @Autowired
    lateinit var pdfFormatExtractor: IDocumentFormatExtractor

    @Autowired
    lateinit var persistManager: IDocumentPersistManager

    @Value("\${bot.message.max-size}")
    lateinit var messageSize: String


    fun getNextTextPartAndPosition(fileInfo: FileInfo): String {
        val file = persistManager.uploadFromStorage(fileInfo)
        val numberOfPages = pdfFormatExtractor.numberOfPages(file)
        var position = fileInfo.lastSentPage.letterPosition
        var page = fileInfo.lastSentPage.page
        var nextPages = mutableListOf<Pair<Int, String>>()
        var header = "${fileInfo.fileName}  "
        //header example: mylovelybook.pdf pages.1765-1768
        val messageSizeMinusHeader = (messageSize.toInt().minus(header.length.plus(7 + (numberOfPages.toString().length) * 2)))

        //TODO: Maybe decrease text size to 4096/2=2048 or lower, that it can fit on screen
        //add new pages to list
        nextPages.add(Pair(page, pdfFormatExtractor.extractTextFromDocumentPage(file, page).substring(position)))
        while (nextPages.asSequence()
                        .map { it.second.length }
                        .reduce { e1, e2 -> e1 + e2 } < messageSizeMinusHeader && page < numberOfPages) {
            page++
            nextPages.add(Pair(page, pdfFormatExtractor.extractTextFromDocumentPage(file, page)))
        }


        if (nextPages.asSequence().map { it.second.length }.reduce { e1, e2 -> e1 + e2 } > messageSizeMinusHeader) {
            //fit last string
            val lastPageSubstringEndIndex = nextPages.last().second.length -
                    (nextPages.asSequence().map { it.second.length }.reduce { e1, e2 -> e1 + e2 } - messageSizeMinusHeader)
            nextPages[nextPages.size - 1] = Pair(nextPages.last().first,
                    nextPages.last().second.substring(0, lastPageSubstringEndIndex))

            //fit to the first punctuation
            nextPages = nextPages.trimToPunctuation(messageSizeMinusHeader)
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
        //TODO: Try do not return FileInfo, only text
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

    /*fun MutableList<Pair<Int, String>>.reduceToSize2(size: Int) : MutableList<Pair<Int, String>> =
            when{
                this.lastOrNull()==null -> this
                this.last().second.isEmpty() -> this.subList(0,this.size-1).reduceToSize2(size)
                this.last().second.endsWith(".")
                        .and(this.asSequence().map { it.second.length }.reduce{e1, e2 -> e1 + e2}>size).not()
                -> {this.subList(0,this.size-1).add(Pair(this.last().first,this.last().second.dropLastWhile { checkStopSymbol.test(this.last().second)
                        .or(this.asSequence().map { it.second.length }.reduce{e1, e2 -> e1 + e2}>size)}))
                    this
                }//change
                else -> this
            }*/
}