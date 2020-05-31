package org.stream.bot.services.impl.commands

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.stream.bot.Bot
import org.stream.bot.services.ICommandHandler
import org.stream.bot.services.IDocumentPersistManager
import org.stream.bot.services.IChatService
import org.stream.bot.services.MARKDOWN_ENABLED
import org.stream.bot.utils.BotConstants
import org.stream.bot.utils.KeyboardFactory
import org.stream.bot.utils.States
import org.stream.bot.utils.Subscribers
import org.telegram.abilitybots.api.util.AbilityUtils
import org.telegram.abilitybots.api.util.AbilityUtils.getLocalizedMessage
import org.telegram.abilitybots.api.util.AbilityUtils.getUser
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.Update

@Service
class RemoveBookCommandHandler : ICommandHandler {

    private val logger = LoggerFactory.getLogger(javaClass)

    @Autowired
    lateinit var bot: Bot

    @Autowired
    lateinit var chatService: IChatService

    @Autowired
    lateinit var persistManager: IDocumentPersistManager

    override fun answer(update: Update) {
        chatService.getUserByIdAndSubscriber(AbilityUtils.getChatId(update).toString(), Subscribers.TELEGRAM).subscribe(
                { chat ->
                    if (chat != null) {
                        val sendMessage = SendMessage()
                                .setChatId(AbilityUtils.getChatId(update))
                                .enableMarkdown(MARKDOWN_ENABLED)
                        if (chat.fileList.isEmpty()) {
                            sendMessage.setText(getLocalizedMessage("remove.book.command.have.no.books",
                                    getUser(update).languageCode).botText())
                        } else {
                            sendMessage.setText(getLocalizedMessage("remove.book.command.what.to.delete",
                                    getUser(update).languageCode).botText())
                                    .setReplyMarkup(KeyboardFactory.inlineBookDeleteKeyboardFromList(chat.fileList))
                            bot.rewriteValueInMapEntry(BotConstants.CHAT_STATES, AbilityUtils.getChatId(update).toString(),
                                    States.WAIT_FOR_REMOVE.toString())
                        }
                        bot.execute(sendMessage)
                    }
                },
                { e -> logger.error(e.message) }
        )

    }

    override fun firstReply(update: Update) {
        chatService.getUserByIdAndSubscriber(AbilityUtils.getChatId(update).toString(), Subscribers.TELEGRAM).subscribe(
                { chat ->
                    if (chat != null) {
                        val removedFileInfo = chat.fileList.find { fileInfo -> fileInfo.checksum.equals(update.callbackQuery.data) }
                        if (removedFileInfo != null && chat.fileList.remove(removedFileInfo)) {
                            chatService.saveUser(chat).subscribe {
                                if (persistManager.removeFromStorage(removedFileInfo)) {
                                    bot.execute(SendMessage()
                                            .setText(getLocalizedMessage("remove.book.command.success.remove",
                                                    getUser(update).languageCode,removedFileInfo.fileName).botText())
                                            .setChatId(AbilityUtils.getChatId(update)))
                                }
                            }
                            bot.rewriteValueInMapEntry(BotConstants.CHAT_STATES,
                                    AbilityUtils.getChatId(update).toString(),
                                    States.WAIT_FOR_BOOK.toString())
                        }
                    }
                },
                { e -> logger.error(e.message) })

    }

    override fun secondReply(update: Update) {
        TODO("Not yet implemented")
    }

    override fun thirdReply(update: Update) {
        TODO("Not yet implemented")
    }
}