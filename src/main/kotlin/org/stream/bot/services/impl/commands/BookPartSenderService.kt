package org.stream.bot.services.impl.commands

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.telegram.telegrambots.meta.generics.TelegramClient
import org.stream.bot.entities.FileInfo
import org.stream.bot.services.IChatService
import org.stream.bot.services.MARKDOWN_ENABLED
import org.stream.bot.services.impl.TextPartExtractorService
import org.stream.bot.utils.KeyboardFactory
import org.stream.bot.utils.Subscribers
import org.telegram.telegrambots.abilitybots.api.util.AbilityUtils
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.exceptions.TelegramApiException

@Service
class BookPartSenderService {

    private val logger = LoggerFactory.getLogger(javaClass)

    @Autowired
    lateinit var textPartExtractorService: TextPartExtractorService

    @Autowired
    lateinit var telegramClient: TelegramClient

    @Autowired
    lateinit var chatService: IChatService


    fun String?.makeBold(): String {
        return "```$this```"
    }

    fun sendMessageAndUpdateFileInfo(fileInfo: FileInfo, chatId: String) {
        try {
            val text = textPartExtractorService.getNextTextPartAndPosition(fileInfo)
            val message = SendMessage.builder()
                    .text(text)
                    .chatId(chatId)
                    .replyMarkup(KeyboardFactory.inlineBookGiveMoreKeyboard(fileInfo))
                    .build()
            message.enableMarkdown(MARKDOWN_ENABLED)
            message.disableWebPagePreview()
            message.enableNotification()
            telegramClient.execute(message)
        } catch (e: TelegramApiException) {
            logger.error(e.message)
            e.printStackTrace()
        }
    }

    fun replyOnNextPageCallback(update: Update) {
        chatService.getUserByIdAndSubscriber(AbilityUtils.getChatId(update).toString(), Subscribers.TELEGRAM).subscribe(
                { chat ->
                    val callback = update.callbackQuery.data.substring(8, update.callbackQuery.data.length)
                    if (chat.fileList.asSequence().filter { it.stillReading }.any { it.checksum.equals(callback,true) }) {
                        val fileinfo = chat.fileList.asSequence()
                                .first { fileInfo -> fileInfo.checksum.equals(callback,true) }
                        sendMessageAndUpdateFileInfo(fileinfo, AbilityUtils.getChatId(update).toString())
                        chatService.saveUser(chat).subscribe()
                    }
                },
                { e -> logger.error(e.message) }
        )
    }
}