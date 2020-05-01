package org.stream.bot.services

import org.stream.bot.entities.DocumentPosition
import org.stream.bot.entities.FileInfo
import java.io.File
import java.io.IOException


interface IDocumentFormatExtractor {

    fun getDocumentFormat(): String

    @Throws(IOException::class)
    fun extractTextFromDocument(file: File, startPage: Int, endPage: Int): String

    fun numberOfPages(file: File): Int
}