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
import java.util.Optional


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
        val chatId = AbilityUtils.getChatId(update).toString()
        val languageCode = getUser(update).languageCode
        val sendMessage = SendMessage()
        sendMessage.chatId = chatId
        sendMessage.enableMarkdown(MARKDOWN_ENABLED)

        //Check if book limit not reached
        chatService.getUserByIdAndSubscriber(chatId, Subscribers.TELEGRAM)
                .map { Optional.of(it) }
                .defaultIfEmpty(Optional.empty())
                .subscribe(
                        { optionalChat ->
                            if (!optionalChat.isPresent) {
                                //Chat is not registered yet (user never called /start)
                                sendMessage.text = getLocalizedMessage("addbook.command.chat.not.found",
                                        languageCode).botText()
                            } else if (optionalChat.get().fileList.size >= optionalChat.get().quantityBookLimit) {
                                sendMessage.text = getLocalizedMessage("addbook.command.book.limit.exceeded",
                                        languageCode, optionalChat.get().quantityBookLimit).botText()
                            } else {
                                sendMessage.text = getLocalizedMessage("addbook.command.send.me.book",
                                        languageCode).botText()
                                sendMessage.replyMarkup = ForceReplyKeyboard.builder().forceReply(true).build()
                                bot.rewriteValueInMapEntry(BotConstants.CHAT_STATES, chatId,
                                        States.WAIT_FOR_BOOK.toString())
                            }
                            executeSafely(sendMessage)
                        },
                        { t ->
                            logger.error(t.message)
                            sendMessage.text = getLocalizedMessage("error.message.something.wrong.on.server",
                                    languageCode).botText()
                            executeSafely(sendMessage)
                        }
                )
    }

    private fun executeSafely(sendMessage: SendMessage) {
        try {
            bot.execute(sendMessage)
        } catch (e: TelegramApiException) {
            logger.error(e.message)
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
        val sendMessage = SendMessage()
        sendMessage.chatId = AbilityUtils.getChatId(update).toString()
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
                val loadingSendMessage = SendMessage.builder()
                        .chatId(sendMessage.chatId)
                        .text(loadingMessage.botText())
                        .replyMarkup(KeyboardFactory.removeKeyboard())
                        .build()
                loadingSendMessage.enableMarkdown(MARKDOWN_ENABLED)
                bot.execute(loadingSendMessage)
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
            if (!sendText.isNullOrEmpty()) {
                sendMessage.text = sendText.botText()
                sendMessage.replyMarkup = KeyboardFactory.removeKeyboard()
                sendMessage.enableMarkdown(MARKDOWN_ENABLED)
                executeSafely(sendMessage)
            }
        }
        return fileInfo
    }
}