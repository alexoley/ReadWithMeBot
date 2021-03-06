package org.stream.bot.services

import org.stream.bot.entities.DocumentPosition
import org.stream.bot.entities.FileInfo
import java.io.File
import java.io.IOException


interface IDocumentFormatExtractor {

    //Must return format as in telegram
    fun getDocumentMimeType(): String

    @Throws(IOException::class)
    fun extractTextFromDocumentPage(file: File, page: Int): String

    fun numberOfPages(file: File): Int
}