package org.stream.bot.services.impl.commands

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.telegram.telegrambots.meta.generics.TelegramClient
import org.stream.bot.services.ChatStateStore
import org.stream.bot.services.ICommandHandler
import org.stream.bot.utils.BotConstants
import org.stream.bot.utils.KeyboardFactory
import org.stream.bot.utils.States
import org.telegram.telegrambots.abilitybots.api.util.AbilityUtils
import org.telegram.telegrambots.abilitybots.api.util.AbilityUtils.getLocalizedMessage
import org.telegram.telegrambots.abilitybots.api.util.AbilityUtils.getUser
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.exceptions.TelegramApiException

@Service
class CancelCommandHandler : ICommandHandler {

    private val logger = LoggerFactory.getLogger(javaClass)

    @Autowired
    lateinit var telegramClient: TelegramClient

    @Autowired
    lateinit var chatStates: ChatStateStore

    override fun answer(update: Update) {
        try {
            if (chatStates.isInState(AbilityUtils.getChatId(update).toString(), States.WAIT_FOR_BOOK)) {
                chatStates.setState(AbilityUtils.getChatId(update).toString(), States.NOT_WAITING)
                val message = SendMessage.builder()
                        .text(getLocalizedMessage("cancel.command.action.canceled",
                                getUser(update).languageCode).botText())
                        .chatId(AbilityUtils.getChatId(update).toString())
                        .replyMarkup(KeyboardFactory.removeKeyboard())
                        .build()
                message.enableMarkdown(true)
                telegramClient.execute(message)
            }

        } catch (e: TelegramApiException) {
            e.printStackTrace()
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