package org.stream.bot.services

import org.stream.bot.entities.FileInfo
import org.telegram.telegrambots.meta.api.objects.Update
import java.io.File

interface IDocumentPersistManager{

    //TODO: Add function remove file from storage
    fun persistToStorage(update: Update, filenameGenerated: String, booksList: ArrayList<FileInfo>): FileInfo

    fun downloadFromStorage(fileInfo: FileInfo): File

    fun removeFromStorage(fileInfo: FileInfo): Boolean
}