package org.stream.bot.services.impl

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Primary
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service
import org.stream.bot.Bot
import org.stream.bot.entities.FileInfo
import org.stream.bot.exceptions.DublicateBookException
import org.stream.bot.exceptions.QuantityLimitBookException
import org.stream.bot.services.IDocumentPersistManager
import org.telegram.telegrambots.meta.api.methods.GetFile
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.exceptions.TelegramApiException
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.security.MessageDigest
import javax.annotation.PostConstruct


@Primary
@Service("localPersistManager")
@Profile("local")
class LocalDocumentTelegramPersistManagerWithInputStream : IDocumentPersistManager {

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

    @Throws(TelegramApiException::class, DublicateBookException::class, QuantityLimitBookException::class, IOException::class)
    override fun persistToStorage(update: Update, filenameGenerated: String, booksList: ArrayList<FileInfo>): FileInfo {
        val telegramFileStream = bot.downloadFileAsStream(downloadTelegramFileWithId(update.message.document.fileId))
        val localPath = bookDirectory + "/" + filenameGenerated
        val localFile = File(localPath)
        val fileChecksum = writeInputStreamToFileAndGetItChecksum(telegramFileStream, localFile)
        logger.debug("Digest: ${fileChecksum}")
        //Check if book already present in user book list by its checksum
        if (booksList.stream()?.anyMatch { fileInfo -> fileInfo.checksum.equals(fileChecksum,ignoreCase = true) }!!) {
            localFile.delete()
            throw DublicateBookException()
        }
        return FileInfo(absolutePath = localFile.absolutePath,
                relativePath = localPath,
                fileName = update.message.document.fileName,
                fileNameInSystem = filenameGenerated,
                mimeType = update.message.document.mimeType,
                fileSize = update.message.document.fileSize,
                checksum = fileChecksum)

    }

    override fun uploadFromStorage(fileInfo: FileInfo): File {
        return File(fileInfo.relativePath)
    }

    override fun removeFromStorage(fileInfo: FileInfo): Boolean {
        TODO("Not yet implemented")
    }

    @Throws(TelegramApiException::class)
    private fun downloadTelegramFileWithId(fileId: String): org.telegram.telegrambots.meta.api.objects.File {
        return bot.execute(GetFile().setFileId(fileId))
    }


    private fun writeInputStreamToFileAndGetItChecksum(inputStream: InputStream, localFile: File): String {
        val localFileOS = localFile.outputStream()
        //Use MD5 algorithm
        val md5Digest = MessageDigest.getInstance("MD5")
        logger.debug("Telegram file available: ${inputStream.available()}")
        inputStream
                .use {
                    //TODO: Make IO async using NIO
                    val buffer = ByteArray(1024)
                    var length = 0
                    while (it.read(buffer).also({ length = it }) != -1) {
                        md5Digest.update(buffer, 0, length)
                        localFileOS.write(buffer, 0, length)
                    }
                }
        localFileOS.close()
        val fileChecksum = bytesToHex(md5Digest.digest())
        return fileChecksum
    }

    private fun bytesToHex(hash: ByteArray): String {
        val hexString = StringBuffer()
        for (i in hash.indices) {
            val hex = Integer.toHexString(0xff and hash[i].toInt())
            if (hex.length == 1) hexString.append('0')
            hexString.append(hex)
        }
        return hexString.toString()
    }
}