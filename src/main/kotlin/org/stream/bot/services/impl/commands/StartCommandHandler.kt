package org.stream.bot.services.impl.commands

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.stream.bot.Bot
import org.stream.bot.entities.User
import org.stream.bot.services.ICommandHandler
import org.stream.bot.services.IUserService
import org.stream.bot.services.MARKDOWN_ENABLED
import org.stream.bot.utils.Subscribers
import org.telegram.abilitybots.api.util.AbilityUtils
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


    override fun answer(update: Update) {
        try {
            userService.saveUserIfNotExist(User(id = update.message.from.id.toString(),
                    subscriber = Subscribers.TELEGRAM,
                    firstName = update.message.from.firstName,
                    lastName = update.message.from.lastName,
                    nickname = update.message.from.userName))
            val text = "It’s nice to meet you, ${update.message.from.firstName}!\uD83D\uDE0A\n" +
                    "Welcome to ReadWithMe Bot. I'm here to help you read books\n" +
                    "I'll send you the part of book each day, that you'd like to read.\uD83D\uDCD6"
            bot.execute(SendMessage().setText(text.botText())
                    .enableMarkdown(MARKDOWN_ENABLED)
                    .setChatId(AbilityUtils.getChatId(update)))
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