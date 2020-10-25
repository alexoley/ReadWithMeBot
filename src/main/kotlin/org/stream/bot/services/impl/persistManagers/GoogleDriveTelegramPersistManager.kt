package org.stream.bot.services.impl.persistManagers

import com.google.api.client.http.InputStreamContent
import com.google.api.services.drive.Drive
import org.apache.commons.io.FileUtils
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service
import org.stream.bot.Bot
import org.stream.bot.entities.FileInfo
import org.stream.bot.exceptions.DublicateBookException
import org.stream.bot.services.IDocumentPersistManager
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.exceptions.TelegramApiException
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException


@Service
@Profile("production")
class GoogleDriveTelegramPersistManager : IDocumentPersistManager, AbstractTelegramPersistManager() {

    private val logger = LoggerFactory.getLogger(javaClass)

    @Autowired
    lateinit var googleDriveService: Drive

    @Autowired
    override lateinit var bot: Bot

    @Throws(TelegramApiException::class, DublicateBookException::class, IOException::class)
    override fun persistToStorage(update: Update, filenameGenerated: String, booksList: ArrayList<FileInfo>): FileInfo {
        val telegramFileStream = bot.downloadFileAsStream(downloadTelegramFileWithId(update.message.document.fileId))
        val fileMetadata = com.google.api.services.drive.model.File()
        fileMetadata.name = filenameGenerated
        var file = com.google.api.services.drive.model.File()
        telegramFileStream.use {
            val mediaContent = InputStreamContent(update.message.document.mimeType, it)
            file = googleDriveService.files().create(fileMetadata, mediaContent)
                    .setFields("id,md5Checksum")
                    .execute()
        }
        if (booksList.stream().anyMatch { fileInfo -> fileInfo.checksum.equals(file.md5Checksum, ignoreCase = true) }) {
            googleDriveService.files().delete(file.id).execute()
            throw DublicateBookException()
        }
        return FileInfo(absolutePath = file.id,
                relativePath = file.id,
                fileName = update.message.document.fileName,
                fileNameInSystem = filenameGenerated,
                mimeType = update.message.document.mimeType,
                fileSize = update.message.document.fileSize,
                checksum = file.md5Checksum)
    }

    override fun downloadFromStorage(fileInfo: FileInfo): File {
        val outputStream = ByteArrayOutputStream()
        googleDriveService.files().get(fileInfo.relativePath)
                .executeMediaAndDownloadTo(outputStream)
        val tempFile = File.createTempFile("bookfile-", ".tmp")
        tempFile.deleteOnExit()
        outputStream.use {
            FileUtils.writeByteArrayToFile(tempFile, it.toByteArray())
        }
        logger.info(tempFile.absolutePath)
        return tempFile
    }

    override fun removeFromStorage(fileInfo: FileInfo): Boolean {
        val response = googleDriveService.files().delete(fileInfo.relativePath).executeUnparsed()
        if (response.statusCode == 204)
            return true
        return false
    }
}