package org.stream.bot.services

import java.io.File
import java.io.IOException


interface IDocumentFormatExtractor {

    fun getDocumentFormat(): String

    @Throws(IOException::class)
    fun extractTextFromDocument(file: File): String
}