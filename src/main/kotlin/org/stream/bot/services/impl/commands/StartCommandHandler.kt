package org.stream.bot.services.impl.commands

import org.apache.tika.parser.txt.CharsetDetector
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.stream.bot.Bot
import org.stream.bot.entities.User
import org.stream.bot.services.ICommandHandler
import org.stream.bot.services.IUserService
import org.stream.bot.services.MARKDOWN_ENABLED
import org.stream.bot.utils.Subscribers
import org.telegram.abilitybots.api.util.AbilityUtils.*
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.exceptions.TelegramApiException


@Service
class StartCommandHandler: ICommandHandler {

    private val logger = LoggerFactory.getLogger(javaClass)

    @Autowired
    lateinit var bot: Bot

    @Autowired
    lateinit var userService: IUserService


    @ExperimentalStdlibApi
    override fun answer(update: Update) {
        try {
            userService.saveUserIfNotExist(User(id = update.message.from.id.toString(),
                    subscriber = Subscribers.TELEGRAM,
                    firstName = update.message.from.firstName,
                    lastName = update.message.from.lastName,
                    nickname = update.message.from.userName))
            val text = getLocalizedMessage("start.command.answer",
                    getUser(update).languageCode, update.message.from.firstName)
            bot.execute(SendMessage().setText(text.botText())
                    .enableMarkdown(MARKDOWN_ENABLED)
                    .setChatId(getChatId(update)))
            //TODO: Good morning/Good afternoon/Good evening
        } catch (e: TelegramApiException) {

        }
    }

    override fun firstReply(update: Update) {
        TODO("Not yet implemented")
    }

    override fun secondReply(update: Update) {
        TODO("Not yet implemented")
    }

    override fun thirdReply(update: Update) {
        TODO("Not yet implemented")
    }
}