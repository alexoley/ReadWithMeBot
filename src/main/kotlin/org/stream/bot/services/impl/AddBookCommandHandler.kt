package org.stream.bot.services.impl

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.stream.bot.Bot
import org.stream.bot.entities.User
import org.stream.bot.exceptions.DublicateBookException
import org.stream.bot.exceptions.QuantityLimitBookException
import org.stream.bot.services.IDocumentFormatExtractor
import org.stream.bot.services.ICommandHandler
import org.stream.bot.services.IDocumentPersistManager
import org.stream.bot.services.IUserService
import org.stream.bot.utils.BotConstants
import org.stream.bot.utils.KeyboardFactory
import org.stream.bot.utils.States
import org.stream.bot.utils.Subscribers
import org.telegram.abilitybots.api.bot.AbilityBot
import org.telegram.abilitybots.api.db.DBContext
import org.telegram.abilitybots.api.sender.MessageSender
import org.telegram.abilitybots.api.util.AbilityUtils
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.methods.send.SendSticker
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.exceptions.TelegramApiException


@Service
class AddBookCommandHandler: ICommandHandler {

    private val logger = LoggerFactory.getLogger(javaClass)

    @Autowired
    private lateinit var documentFormatExtractorList: List<IDocumentFormatExtractor>

    @Autowired
    private lateinit var documentPersistManager: IDocumentPersistManager

    @Autowired
    private lateinit var bot: Bot

    @Autowired
    private lateinit var userService: IUserService

    override fun process(update: Update) {
        val sendMessage: SendMessage = SendMessage().setChatId(AbilityUtils.getChatId(update))
        try {
            //If document format not supports
            if (documentFormatExtractorList.stream()
                            .noneMatch { it.getDocumentFormat().equals(update.message.document.mimeType) }) {
                sendMessage.setText("I do not yet support this document format.")
                return
            }
            val filename = System.currentTimeMillis().toString() + "_" + update.message.document.fileName
            documentPersistManager.persistToStorage(update, filename)
            //TODO: Add filesize check and dublicate file check
            //saveFileInfo(FileInfo fileinfo);
            //userService.addBookIfNotExists(User(update.message.from.id.toString(),subscriber = Subscribers.TELEGRAM),update)
            sendMessage.setText("You've send me " +
                            update.message.document.fileName +
                            ". I wish you an exciting read.")
        }
        catch (e: DublicateBookException){
            sendMessage.setText("You have already added this book.")
        }
        catch(e: QuantityLimitBookException){
            sendMessage.setText("Your limit on the number of books is ${e.limit}.\nYou cannot exceed it.")
        }
        catch (e: TelegramApiException) {
            if(update.message?.document?.fileSize!!>BotConstants.MAX_TELEGRAM_FILE_SIZE){
                sendMessage.setText("Book size is too large.")
            }
        }
        catch (e: NullPointerException){
            e.printStackTrace()
        }
        finally {
            logger.info("Finally block executes")
            bot.rewriteValueInMapEntry(BotConstants.CHAT_STATES,
                    AbilityUtils.getChatId(update).toString(),
                    States.NOT_WAITING_FOR_BOOK.toString())
            bot.execute(sendMessage
                    .setReplyMarkup(KeyboardFactory.removeKeyboard()))
        }
    }
}