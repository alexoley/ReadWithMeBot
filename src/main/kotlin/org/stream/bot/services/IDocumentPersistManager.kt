package org.stream.bot.services

import org.stream.bot.Bot
import org.stream.bot.entities.FileInfo
import org.telegram.telegrambots.meta.api.methods.GetFile
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.exceptions.TelegramApiException
import java.io.File

interface IDocumentPersistManager{

    fun persistToStorage(update: Update, filenameGenerated: String, booksList: ArrayList<FileInfo>): FileInfo

    fun downloadFromStorage(fileInfo: FileInfo): File

    fun removeFromStorage(fileInfo: FileInfo): Boolean
}