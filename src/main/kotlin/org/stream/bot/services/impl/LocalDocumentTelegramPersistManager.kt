package org.stream.bot.services.impl

import org.apache.commons.codec.digest.DigestUtils
import org.apache.commons.io.FileUtils
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Primary
import org.springframework.stereotype.Service
import org.stream.bot.Bot
import org.stream.bot.services.IDocumentPersistManager
import org.telegram.abilitybots.api.sender.MessageSender
import org.telegram.telegrambots.meta.api.methods.GetFile
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.exceptions.TelegramApiException
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.nio.file.Files
import java.security.DigestInputStream
import java.security.MessageDigest


@Service
class LocalDocumentTelegramPersistManager: IDocumentPersistManager {

    private val logger = LoggerFactory.getLogger(javaClass)

    @Autowired
    lateinit var bot: Bot

    override fun persistToStorage(update: Update, filename: String){
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