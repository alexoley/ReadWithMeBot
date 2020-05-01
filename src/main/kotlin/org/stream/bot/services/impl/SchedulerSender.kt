package org.stream.bot.services.impl

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.stream.bot.Bot
import org.stream.bot.entities.FileInfo
import org.stream.bot.entities.User
import org.stream.bot.services.IUserService
import org.stream.bot.services.impl.document.extractors.PdfFormatExtractor
import org.stream.bot.services.impl.document.extractors.TextPartExtractorService
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.exceptions.TelegramApiException
import java.util.function.Consumer

@Service
class SchedulerSender {

    private val logger = LoggerFactory.getLogger(javaClass)

    @Autowired
    lateinit var userService: IUserService

    @Autowired
    lateinit var textPartExtractorService: TextPartExtractorService

    @Autowired
    lateinit var bot: Bot

    fun String?.makeBold(): String {
        return "```$this```"
    }

    companion object {
        private const val cronexpression = "0 10 10 10 * ?"
    }

    private fun sendMessageAndUpdate(fileInfo: FileInfo, chatId: String) {
        val pair = textPartExtractorService.getNextTextPartAndPosition(fileInfo)
        try {
            bot.execute(SendMessage().setText(pair.first)
                    .setChatId(chatId)
                    .enableMarkdown(false)
                    .disableWebPagePreview())
            fileInfo.lastSentPage = pair.second
        } catch (e: TelegramApiException) {
            logger.error(e.message)
        }
    }

    //@Scheduled(cron = cronexpression)
    fun scheduler() {
        logger.info("Scheduler sender run.")
        userService.getAllUsers()
                .filter { user ->
                    if (user != null) user.fileList.isNullOrEmpty().not() else false
                }
                .subscribe(
                        Consumer {
                            it.fileList.forEach(Consumer { t -> sendMessageAndUpdate(t, it.id) })
                            userService.saveUser(it).subscribe()
                        },
                        Consumer { t -> logger.error(t.message) })
    }
}