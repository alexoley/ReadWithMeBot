package org.stream.bot.services.impl.commands

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.stream.bot.Bot
import org.stream.bot.services.ICommandHandler
import org.stream.bot.utils.BotConstants
import org.stream.bot.utils.KeyboardFactory
import org.stream.bot.utils.States
import org.telegram.abilitybots.api.util.AbilityUtils
import org.telegram.abilitybots.api.util.AbilityUtils.getLocalizedMessage
import org.telegram.abilitybots.api.util.AbilityUtils.getUser
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.exceptions.TelegramApiException

@Service
class CancelCommandHandler : ICommandHandler {

    private val logger = LoggerFactory.getLogger(javaClass)

    @Autowired
    lateinit var bot: Bot

    override fun answer(update: Update) {
        try {
            if (bot.isValueEqualsInMapEntry(BotConstants.CHAT_STATES,
                            AbilityUtils.getChatId(update).toString(),
                            States.WAIT_FOR_BOOK.toString())) {
                bot.rewriteValueInMapEntry(BotConstants.CHAT_STATES,
                        AbilityUtils.getChatId(update).toString(),
                        States.NOT_WAITING.toString())
                val message = SendMessage.builder()
                        .text(getLocalizedMessage("cancel.command.action.canceled",
                                getUser(update).languageCode).botText())
                        .chatId(AbilityUtils.getChatId(update).toString())
                        .replyMarkup(KeyboardFactory.removeKeyboard())
                        .build()
                message.enableMarkdown(true)
                bot.execute(message)
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