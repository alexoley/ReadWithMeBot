package org.stream.bot.services.impl.commands

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.stream.bot.Bot
import org.stream.bot.entities.Chat
import org.stream.bot.entities.FileInfo
import org.stream.bot.exceptions.DublicateBookException
import org.stream.bot.services.*
import org.stream.bot.utils.BotConstants
import org.stream.bot.utils.KeyboardFactory
import org.stream.bot.utils.States
import org.stream.bot.utils.Subscribers
import org.telegram.abilitybots.api.util.AbilityUtils
import org.telegram.abilitybots.api.util.AbilityUtils.getLocalizedMessage
import org.telegram.abilitybots.api.util.AbilityUtils.getUser
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ForceReplyKeyboard
import org.telegram.telegrambots.meta.exceptions.TelegramApiException
import java.io.IOException


@Service
class AddBookCommandHandler : ICommandHandler {

    private val logger = LoggerFactory.getLogger(javaClass)

    @Autowired
    lateinit var documentFormatExtractorList: List<IDocumentFormatExtractor>

    @Autowired
    lateinit var documentPersistManager: IDocumentPersistManager

    @Autowired
    lateinit var bot: Bot

    @Autowired
    lateinit var chatService: IChatService

    @Autowired
    lateinit var bookPartSenderService: BookPartSenderService

    override fun answer(update: Update) {
        try {
            val sendMessage = SendMessage()
                    .setChatId(AbilityUtils.getChatId(update))
                    .enableMarkdown(MARKDOWN_ENABLED)

            //Check if book limit not reached
            //val user = userService.getUserByIdAndSubscriber(AbilityUtils.getChatId(update).toString(), Subscribers.TELEGRAM).awaitFirst()
            chatService.getUserByIdAndSubscriber(AbilityUtils.getChatId(update).toString(), Subscribers.TELEGRAM).subscribe(
                    { chat ->
                        if (chat.fileList.size >= chat.quantityBookLimit) {
                            //sendMessage.setText("You already reached your book limit.\uD83D\uDE14".botText())
                            //sendText="Your limit on the number of books is ${e.limit}.\nYou cannot exceed it."
                            sendMessage.setText(getLocalizedMessage("addbook.command.book.limit.exceeded",
                                    getUser(update).languageCode, chat.quantityBookLimit).botText())
                        } else {
                            sendMessage.setText(getLocalizedMessage("addbook.command.send.me.book",
                                    getUser(update).languageCode).botText())
                                    .setReplyMarkup(KeyboardFactory.cancelButton(getLocalizedMessage("cancel",
                                            getUser(update).languageCode)))
                                    .setReplyMarkup(ForceReplyKeyboard())
                            //db.getMap<Any, Any>(BotConstants.CHAT_STATES)[ctx.chatId().toString()] = States.WAIT_FOR_BOOK
                            bot.rewriteValueInMapEntry(BotConstants.CHAT_STATES,
                                    AbilityUtils.getChatId(update).toString(),
                                    States.WAIT_FOR_BOOK.toString())
                        }
                    },
                    { t ->
                        sendMessage.setText(getLocalizedMessage("error.message.something.wrong.on.server",
                                getUser(update).languageCode).botText())
                        logger.error(t.message)
                    }
                    , { bot.execute(sendMessage) }
            )
        } catch (e: TelegramApiException) {
            e.printStackTrace()
        }
    }


    override fun firstReply(update: Update) {
        chatService.getUserByIdAndSubscriber(AbilityUtils.getChatId(update).toString(), Subscribers.TELEGRAM).subscribe(
                { chat ->
                    if (chat != null) {
                        val lastFileInfo : FileInfo? = process(update, chat)
                        if(lastFileInfo!=null) {
                            bookPartSenderService.sendMessageAndUpdateFileInfo(lastFileInfo, AbilityUtils.getChatId(update).toString())
                        }
                        chatService.saveUser(chat).subscribe()
                    }
                },
                { t -> logger.error(t.message) }
        )
    }

    override fun secondReply(update: Update) {
        TODO("Not yet implemented")
    }

    override fun thirdReply(update: Update) {
        TODO("Not yet implemented")
    }

    private fun process(update: Update, monoChat: Chat): FileInfo? {
        var fileInfo : FileInfo? = null
        val sendMessage: SendMessage = SendMessage().setChatId(AbilityUtils.getChatId(update))
        var sendText: String? = ""
        try {

            //Check if book is too large
            if (update.message?.document?.fileSize!! > BotConstants.MAX_TELEGRAM_FILE_SIZE) {
                sendText = getLocalizedMessage("addbook.command.too.large.book",
                        getUser(update).languageCode)
                return fileInfo
            }

            //If document format not supports
            if (documentFormatExtractorList.stream()
                            .noneMatch { it.getDocumentMimeType().equals(update.message.document.mimeType) }) {
                sendText = getLocalizedMessage("addbook.command.not.support.document.format",
                        getUser(update).languageCode)
                return fileInfo
            }

            bot.rewriteValueInMapEntry(BotConstants.CHAT_STATES,
                    AbilityUtils.getChatId(update).toString(),
                    States.NOT_WAITING.toString())

            //Send loading message if file more than 1 megabyte
            if (update.message.document.fileSize > 1024 * 1024) {
                val loadingMessage = getLocalizedMessage("addbook.command.loading.file",
                        getUser(update).languageCode)
                bot.execute(sendMessage
                        .setText(loadingMessage.botText())
                        .setReplyMarkup(KeyboardFactory.removeKeyboard())
                        .enableMarkdown(MARKDOWN_ENABLED))
            }
            val filenameGenerated = System.currentTimeMillis().toString() + "_" + update.message.document.fileName
            fileInfo = documentPersistManager.persistToStorage(update, filenameGenerated, monoChat.fileList)
            monoChat.fileList.add(fileInfo)
            //chatService.saveUser(monoChat).subscribe()
            sendText = getLocalizedMessage("addbook.command.successful.book.addition",
                    getUser(update).languageCode, update.message.document.fileName)
        } catch (e: DublicateBookException) {
            sendText = getLocalizedMessage("addbook.command.dublicate.book",
                    getUser(update).languageCode)
        } catch (e: TelegramApiException) {
            e.printStackTrace()
        } catch (e: IOException) {
            sendText = getLocalizedMessage("error.message.something.wrong.on.server",
                    getUser(update).languageCode)
            logger.error("Error in file copying(saving)")
            e.printStackTrace()
        } catch (e: NullPointerException) {
            sendText = getLocalizedMessage("error.message.something.wrong.on.server",
                    getUser(update).languageCode)
            logger.error("update.message.document.filesize in null")
            e.printStackTrace()
        } finally {
            bot.execute(sendMessage
                    .setText(sendText.botText())
                    .setReplyMarkup(KeyboardFactory.removeKeyboard())
                    .enableMarkdown(MARKDOWN_ENABLED))
            return fileInfo
        }
    }
}