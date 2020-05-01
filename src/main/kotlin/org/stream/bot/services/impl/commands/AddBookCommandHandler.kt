package org.stream.bot.services.impl.commands

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.stream.bot.Bot
import org.stream.bot.entities.User
import org.stream.bot.exceptions.DublicateBookException
import org.stream.bot.exceptions.QuantityLimitBookException
import org.stream.bot.services.*
import org.stream.bot.utils.BotConstants
import org.stream.bot.utils.KeyboardFactory
import org.stream.bot.utils.States
import org.stream.bot.utils.Subscribers
import org.telegram.abilitybots.api.util.AbilityUtils
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.exceptions.TelegramApiException
import java.io.IOException
import java.util.function.Consumer


@Service
class AddBookCommandHandler : ICommandHandler {

    private val logger = LoggerFactory.getLogger(javaClass)

    @Autowired
    private lateinit var documentFormatExtractorList: List<IDocumentFormatExtractor>

    @Autowired
    private lateinit var documentPersistManager: IDocumentPersistManager

    @Autowired
    private lateinit var bot: Bot

    @Autowired
    private lateinit var userService: IUserService

    override fun answer(update: Update) {
        try {
            bot.execute(SendMessage()
                    .setText("Send me book. The book should be no more than 20 megabytes.".botText())
                    .setChatId(AbilityUtils.getChatId(update))
                    .setReplyMarkup(KeyboardFactory.cancelButton())
                    .enableMarkdown(MARKDOWN_ENABLED))
            //db.getMap<Any, Any>(BotConstants.CHAT_STATES)[ctx.chatId().toString()] = States.WAIT_FOR_BOOK
            bot.rewriteValueInMapEntry(BotConstants.CHAT_STATES,
                    AbilityUtils.getChatId(update).toString(),
                    States.WAIT_FOR_BOOK.toString())
        } catch (e: TelegramApiException) {
            e.printStackTrace()
        }
    }

    override fun firstReply(update: Update) {
        val monoUser = userService.getUserByIdAndSubscriber(update.message.from.id.toString(), Subscribers.TELEGRAM).subscribe(
                Consumer { user ->
                    if (user!=null){
                        process(update, user)
                    }
                },
                Consumer { t -> logger.error(t.message) }
        )
    }

    override fun secondReply(update: Update) {
        TODO("Not yet implemented")
    }

    override fun thirdReply(update: Update) {
        TODO("Not yet implemented")
    }

    private fun process(update: Update, monoUser: User) {
        val sendMessage: SendMessage = SendMessage().setChatId(AbilityUtils.getChatId(update))
        var sendText : String?=""
        try {
            //If document format not supports
            if (documentFormatExtractorList.stream()
                            .noneMatch { it.getDocumentFormat().equals(update.message.document.mimeType) }) {
                sendText = "I do not yet support this document format.\uD83D\uDE36"
                return
            }
            //Check if book limit not reached
            //TODO: Move to first answer(first reply)
            if (monoUser.fileList.count()>=monoUser.quantityBookLimit){
                throw QuantityLimitBookException(monoUser.quantityBookLimit)
            }
            logger.info("File size: ${update.message.document.fileSize}")
            //Send loading message if file more than 1 megabyte
            if(update.message.document.fileSize>1024*1024){
                val loadingMessage = "Loading file... Please, wait"
                bot.execute(sendMessage
                        .setText(loadingMessage.botText())
                        .setReplyMarkup(KeyboardFactory.removeKeyboard())
                        .enableMarkdown(MARKDOWN_ENABLED))
            }
            val filenameGenerated = System.currentTimeMillis().toString() + "_" + update.message.document.fileName
            val fileInfo = documentPersistManager.persistToStorage(update, filenameGenerated, monoUser.fileList)
            monoUser.fileList.add(fileInfo)
            userService.saveUser(monoUser).block()
            sendText = "You've send me " +
                    update.message.document.fileName +
                    ". I wish you an exciting read.\uD83D\uDE0C"
        } catch (e: DublicateBookException) {
            sendText = "You have already added this book.\uD83E\uDD28"
        } catch (e: QuantityLimitBookException) {
            sendText="You already reached your book limit.\uD83D\uDE14"
            //sendText="Your limit on the number of books is ${e.limit}.\nYou cannot exceed it."
        } catch (e: TelegramApiException) {
            if (update.message?.document?.fileSize!! > BotConstants.MAX_TELEGRAM_FILE_SIZE) {
                sendText = "Book size is too large.\uD83D\uDE16"
            }
            e.printStackTrace()
        } catch (e: IOException) {
            sendText = "Something went wrong on server side.\nTry this later.\uD83E\uDD15"
            logger.error("Error in file copying(saving)")
            e.printStackTrace()
        } catch (e: NullPointerException) {
            sendText = "Something went wrong on server side.\nTry this later.\uD83E\uDD15"
            logger.error("update.message.document.filesize in null")
            e.printStackTrace()
        } finally {
            bot.rewriteValueInMapEntry(BotConstants.CHAT_STATES,
                    AbilityUtils.getChatId(update).toString(),
                    States.NOT_WAITING.toString())
            bot.execute(sendMessage
                    .setText(sendText.botText())
                    .setReplyMarkup(KeyboardFactory.removeKeyboard())
                    .enableMarkdown(MARKDOWN_ENABLED))
        }
    }
}