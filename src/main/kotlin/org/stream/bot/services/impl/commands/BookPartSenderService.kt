package org.stream.bot.services.impl.commands

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.stream.bot.Bot
import org.stream.bot.entities.FileInfo
import org.stream.bot.services.IUserService
import org.stream.bot.services.MARKDOWN_ENABLED
import org.stream.bot.services.impl.TextPartExtractorService
import org.stream.bot.utils.KeyboardFactory
import org.stream.bot.utils.Subscribers
import org.telegram.abilitybots.api.util.AbilityUtils
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.exceptions.TelegramApiException
import java.util.function.Consumer

@Service
class BookPartSenderService {

    private val logger = LoggerFactory.getLogger(javaClass)

    @Autowired
    lateinit var textPartExtractorService: TextPartExtractorService

    @Autowired
    lateinit var bot: Bot

    @Autowired
    lateinit var userService: IUserService

    @Autowired
    lateinit var bookPartSenderService: BookPartSenderService

    fun String?.makeBold(): String {
        return "```$this```"
    }

    fun sendMessageAndUpdate(fileInfo: FileInfo, chatId: String) {
        try {
            val text = textPartExtractorService.getNextTextPartAndPosition(fileInfo)
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

    fun replyOnNextPageCallback(update: Update) {
        userService.getUserByIdAndSubscriber(update.callbackQuery.from.id.toString(), Subscribers.TELEGRAM).subscribe(
                Consumer { user ->
                    val callback = update.callbackQuery.data.subSequence(8, update.callbackQuery.data.length)
                    if (user.fileList.asSequence().filter { it.stillReading }.any { it.checksum == callback }) {
                        val fileinfo = user.fileList.asSequence()
                                .first { fileInfo -> fileInfo.checksum.equals(callback) }
                        bookPartSenderService.sendMessageAndUpdate(fileinfo, AbilityUtils.getChatId(update).toString())
                    }
                    userService.saveUser(user).subscribe()
                },
                Consumer { e -> logger.error(e.message) }
        )
    }
}