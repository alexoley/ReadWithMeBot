package org.stream.bot.services.impl

import org.apache.commons.codec.digest.DigestUtils
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Primary
import org.springframework.stereotype.Service
import org.stream.bot.Bot
import org.stream.bot.exceptions.DublicateBookException
import org.stream.bot.exceptions.QuantityLimitBookException
import org.stream.bot.repositories.UserReactiveRepository
import org.stream.bot.services.IDocumentPersistManager
import org.stream.bot.utils.Subscribers
import org.telegram.telegrambots.meta.api.methods.GetFile
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.exceptions.TelegramApiException
import java.io.File
import java.io.IOException
import javax.annotation.PostConstruct

@Primary
@Service
class LocalDocumentTelegramPersistManagerWithInputStream: IDocumentPersistManager {
    private val logger = LoggerFactory.getLogger(javaClass)

    @Autowired
    lateinit var bot: Bot

    @Autowired
    lateinit var userReactiveRepository: UserReactiveRepository

    @Value("\${local.books.directory}")
    lateinit var bookDirectory: String

    @PostConstruct
    fun init(){
        val newDirectory = File(bookDirectory)
        if(newDirectory.exists().not())
            newDirectory.mkdir()

    }

    @Throws(TelegramApiException::class, DublicateBookException::class, QuantityLimitBookException::class)
    override fun persistToStorage(update: Update, filename: String){
        try {
            val monoUser = userReactiveRepository.findByIdAndSubscriber(update.message.from.id.toString(),Subscribers.TELEGRAM)
            val localPath = bookDirectory+"/"+filename
            val localFile = File(localPath)
            bot.downloadFileAsStream(downloadTelegramFileWithId(update.message.document.fileId))
                    .use {
                        val user = monoUser.block()
                        if(user?.fileList?.stream()?.anyMatch
                                { fileInfo -> fileInfo.hash.equals(DigestUtils.md5Hex(it)) }!!){
                            throw DublicateBookException()
                        }
                        if(user.quantityBookLimit<=user.fileList.count()){
                            throw QuantityLimitBookException(user.quantityBookLimit)
                        }
                        it.copyTo(localFile.outputStream())
                    }
        } catch (e: IOException) {
            logger.error("Error in file copying(saving)")
        }
    }

    @Throws(TelegramApiException::class)
    private fun downloadTelegramFileWithId(fileId: String): org.telegram.telegrambots.meta.api.objects.File {
        return bot.execute(GetFile().setFileId(fileId))
    }
}