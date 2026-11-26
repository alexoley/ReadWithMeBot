package org.stream.bot.services.impl.commands

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.telegram.telegrambots.meta.generics.TelegramClient
import org.stream.bot.entities.Chat
import org.stream.bot.services.ICommandHandler
import org.stream.bot.services.IChatService
import org.stream.bot.services.MARKDOWN_ENABLED
import org.stream.bot.utils.*
import org.telegram.telegrambots.abilitybots.api.util.AbilityUtils.*
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.exceptions.TelegramApiException


@Service
class StartCommandHandler : ICommandHandler {

    private val logger = LoggerFactory.getLogger(javaClass)

    @Autowired
    lateinit var telegramClient: TelegramClient

    @Autowired
    lateinit var chatService: IChatService

    @Value("\${bot.username}")
    lateinit var botUsername: String


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
                    getUser(update).languageCode, update.getChatFirstName(),botUsername)
            val message = SendMessage.builder()
                    .text(text.botText())
                    .chatId(getChatId(update).toString())
                    .build()
            message.enableMarkdown(MARKDOWN_ENABLED)
            telegramClient.execute(message)
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