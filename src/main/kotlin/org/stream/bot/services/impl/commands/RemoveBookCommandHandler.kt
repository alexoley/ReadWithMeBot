package org.stream.bot.services.impl.commands

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.stream.bot.Bot
import org.stream.bot.services.ICommandHandler
import org.stream.bot.services.IDocumentPersistManager
import org.stream.bot.services.IUserService
import org.stream.bot.services.MARKDOWN_ENABLED
import org.stream.bot.utils.BotConstants
import org.stream.bot.utils.KeyboardFactory
import org.stream.bot.utils.States
import org.stream.bot.utils.Subscribers
import org.telegram.abilitybots.api.util.AbilityUtils
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.Update
import java.util.function.Consumer

@Service
class RemoveBookCommandHandler : ICommandHandler {

    private val logger = LoggerFactory.getLogger(javaClass)

    @Autowired
    lateinit var bot: Bot

    @Autowired
    lateinit var userService: IUserService

    @Autowired
    lateinit var persistManager: IDocumentPersistManager

    override fun answer(update: Update) {
        //TODO: Refactor this method
        userService.getUserByIdAndSubscriber(update.message.from.id.toString(), Subscribers.TELEGRAM).subscribe(
                Consumer { user ->
                    if (user != null) {
                        if (user.fileList.isEmpty()) {
                            bot.execute(SendMessage()
                                    .setText("You have no books...\nRun /addbook command to resolve it!".botText())
                                    .setChatId(AbilityUtils.getChatId(update))
                                    .enableMarkdown(MARKDOWN_ENABLED))
                        } else {
                            bot.execute(SendMessage()
                                    .setText("What book do you want to delete?".botText())
                                    .setChatId(AbilityUtils.getChatId(update))
                                    .setReplyMarkup(KeyboardFactory.inlineBookDeleteKeyboardFromList(user.fileList))
                                    .enableMarkdown(MARKDOWN_ENABLED))
                            bot.rewriteValueInMapEntry(BotConstants.CHAT_STATES, AbilityUtils.getChatId(update).toString(),
                                    States.WAIT_FOR_REMOVE.toString())
                        }
                    }
                },
                Consumer { e -> logger.error(e.message) }
        )

    }

    override fun firstReply(update: Update) {
        userService.getUserByIdAndSubscriber(AbilityUtils.getChatId(update).toString(), Subscribers.TELEGRAM).subscribe(
                Consumer { user ->
                    if (user != null) {
                        val removedFileInfo = user.fileList.find { fileInfo -> fileInfo.checksum.equals(update.callbackQuery.data) }
                        if (removedFileInfo!=null && user.fileList.remove(removedFileInfo)) {
                            userService.saveUser(user).subscribe {
                                if(persistManager.removeFromStorage(removedFileInfo)){
                                    bot.execute(SendMessage()
                                            .setText("You successfully removed ${removedFileInfo?.fileName}.".botText())
                                            .setChatId(AbilityUtils.getChatId(update))
                                            .enableMarkdown(MARKDOWN_ENABLED))
                                }
                            }
                            bot.rewriteValueInMapEntry(BotConstants.CHAT_STATES,
                                    AbilityUtils.getChatId(update).toString(),
                                    States.WAIT_FOR_BOOK.toString())
                        }
                    }
                },
                Consumer { e -> logger.error(e.message) })

    }

    override fun secondReply(update: Update) {
        TODO("Not yet implemented")
    }

    override fun thirdReply(update: Update) {
        TODO("Not yet implemented")
    }
}