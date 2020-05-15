package org.stream.bot.services.impl

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.stream.bot.Bot
import org.stream.bot.entities.FileInfo
import org.stream.bot.services.MARKDOWN_ENABLED
import org.stream.bot.utils.KeyboardFactory
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.exceptions.TelegramApiException

@Service
class BookPartSender {

    private val logger = LoggerFactory.getLogger(javaClass)

    @Autowired
    lateinit var textPartExtractorService: TextPartExtractorService

    @Autowired
    lateinit var bot: Bot

    fun String?.makeBold(): String {
        return "```$this```"
    }

    fun sendMessageAndUpdate(fileInfo: FileInfo, chatId: String) {
        try {
            val text = textPartExtractorService.getNextTextPartAndPosition(fileInfo)
            //TODO: Add CallbackQuery(Get More, Read More,) to message
            bot.execute(SendMessage().setText(text)
                    .setChatId(chatId)
                    .enableMarkdown(MARKDOWN_ENABLED)
                    .disableWebPagePreview()
                    .enableNotification()
                    .setReplyMarkup(KeyboardFactory.inlineBookGiveMoreKeyboard(fileInfo)))
        } catch (e: TelegramApiException) {
            logger.error(e.message)
            e.printStackTrace()
        }
    }
}