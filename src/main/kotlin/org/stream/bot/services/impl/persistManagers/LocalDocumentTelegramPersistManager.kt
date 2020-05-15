package org.stream.bot.services.impl.persistManagers

import org.apache.commons.codec.digest.DigestUtils
import org.apache.commons.io.FileUtils
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.stream.bot.Bot
import org.stream.bot.entities.FileInfo
import org.telegram.telegrambots.meta.api.methods.GetFile
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.exceptions.TelegramApiException
import org.telegram.telegrambots.meta.updateshandlers.DownloadFileCallback
import java.io.File
import java.io.IOException
import java.lang.Exception
import javax.annotation.PostConstruct

@Deprecated(message = "Not implemented", level = DeprecationLevel.WARNING)
class LocalDocumentTelegramPersistManager {

    private val logger = LoggerFactory.getLogger(javaClass)

    @Autowired
    lateinit var bot: Bot

    @Value("\${local.books.directory}")
    lateinit var bookDirectory: String

    @PostConstruct
    fun init() {
        val newDirectory = File(bookDirectory)
        if (newDirectory.exists().not())
            newDirectory.mkdir()

    }

    val callback = object: DownloadFileCallback<org.telegram.telegrambots.meta.api.objects.File>{
        override fun onResult(file: org.telegram.telegrambots.meta.api.objects.File?, output: File?) {
            TODO("Not yet implemented")
        }

        override fun onException(file: org.telegram.telegrambots.meta.api.objects.File?, exception: Exception?) {
            TODO("Not yet implemented")
        }
    }

    fun persistToStorage(update: Update, filename: String, booksList: ArrayList<FileInfo>){
        try {
            val localFile = File(filename)
            val receivedFile = downloadFileWithId(update.message.document.fileId)
            var md ="";
            println(md)
            receivedFile.inputStream().use{
                md = DigestUtils.md5Hex(it).toUpperCase()
            }
            println(md)
            FileUtils.copyFile(receivedFile, localFile)
        } catch (e: IOException) {
            logger.error("Error in file copying(saving)")
        } catch (e: TelegramApiException) {
            logger.error("Error in downloading file exception: ${e.message}")
        }
    }

    @Throws(TelegramApiException::class)
    private fun downloadFileWithId(fileId: String): File {
        return bot.downloadFile(bot.execute(GetFile().setFileId(fileId)))
    }
}