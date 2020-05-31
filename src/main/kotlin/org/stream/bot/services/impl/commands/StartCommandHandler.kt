package org.stream.bot.services.impl.commands

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.stream.bot.Bot
import org.stream.bot.entities.Chat
import org.stream.bot.services.ICommandHandler
import org.stream.bot.services.IChatService
import org.stream.bot.services.MARKDOWN_ENABLED
import org.stream.bot.utils.*
import org.telegram.abilitybots.api.util.AbilityUtils.*
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.exceptions.TelegramApiException


@Service
class StartCommandHandler : ICommandHandler {

    private val logger = LoggerFactory.getLogger(javaClass)

    @Autowired
    lateinit var bot: Bot

    @Autowired
    lateinit var chatService: IChatService


    @ExperimentalStdlibApi
    override fun answer(update: Update) {
        try {
            chatService.saveUserIfNotExist(Chat(id = getChatId(update).toString(),
                    subscriber = Subscribers.TELEGRAM,
                    firstName = update.getChatFirstName(),
                    lastName = update.getChatLastName(),
                    username = update.getChatUsername(),
                    chatType = update.getChatType()))
            val text = getLocalizedMessage("start.command.answer",
                    getUser(update).languageCode, update.getChatFirstName(),bot.botUsername)
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