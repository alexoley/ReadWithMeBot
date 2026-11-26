package org.stream.bot.services.impl.commands

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.telegram.telegrambots.meta.generics.TelegramClient
import org.stream.bot.services.ChatStateStore
import org.stream.bot.services.ICommandHandler
import org.stream.bot.services.IDocumentPersistManager
import org.stream.bot.services.IChatService
import org.stream.bot.services.MARKDOWN_ENABLED
import org.stream.bot.utils.BotConstants
import org.stream.bot.utils.KeyboardFactory
import org.stream.bot.utils.States
import org.stream.bot.utils.Subscribers
import org.telegram.telegrambots.abilitybots.api.util.AbilityUtils
import org.telegram.telegrambots.abilitybots.api.util.AbilityUtils.getLocalizedMessage
import org.telegram.telegrambots.abilitybots.api.util.AbilityUtils.getUser
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.Update

@Service
class RemoveBookCommandHandler : ICommandHandler {

    private val logger = LoggerFactory.getLogger(javaClass)

    @Autowired
    lateinit var telegramClient: TelegramClient

    @Autowired
    lateinit var chatStates: ChatStateStore

    @Autowired
    lateinit var chatService: IChatService

    @Autowired
    lateinit var persistManager: IDocumentPersistManager

    override fun answer(update: Update) {
        chatService.getUserByIdAndSubscriber(AbilityUtils.getChatId(update).toString(), Subscribers.TELEGRAM).subscribe(
                { chat ->
                    if (chat != null) {
                        val sendMessage = SendMessage(AbilityUtils.getChatId(update).toString(), "")
                        sendMessage.enableMarkdown(MARKDOWN_ENABLED)
                        if (chat.fileList.isEmpty()) {
                            sendMessage.text = getLocalizedMessage("remove.book.command.have.no.books",
                                    getUser(update).languageCode).botText()
                        } else {
                            sendMessage.text = getLocalizedMessage("remove.book.command.what.to.delete",
                                    getUser(update).languageCode).botText()
                            sendMessage.replyMarkup = KeyboardFactory.inlineBookDeleteKeyboardFromList(chat.fileList)
                            chatStates.setState(AbilityUtils.getChatId(update).toString(), States.WAIT_FOR_REMOVE)
                        }
                        telegramClient.execute(sendMessage)
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
                                    telegramClient.execute(SendMessage.builder()
                                            .text(getLocalizedMessage("remove.book.command.success.remove",
                                                    getUser(update).languageCode,removedFileInfo.fileName).botText())
                                            .chatId(AbilityUtils.getChatId(update).toString())
                                            .build())
                                }
                            }
                            chatStates.setState(AbilityUtils.getChatId(update).toString(), States.NOT_WAITING)
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