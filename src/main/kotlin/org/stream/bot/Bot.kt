package org.stream.bot

import com.google.api.services.drive.Drive
import com.google.api.services.drive.model.File
import com.google.api.services.drive.model.FileList
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.stream.bot.services.ICommandHandler
import org.stream.bot.services.IUserService
import org.stream.bot.services.impl.BookPartSender
import org.stream.bot.services.impl.Scheduler
import org.stream.bot.utils.BotConstants
import org.stream.bot.utils.KeyboardFactory
import org.stream.bot.utils.States
import org.stream.bot.utils.Subscribers
import org.telegram.abilitybots.api.bot.AbilityBot
import org.telegram.abilitybots.api.objects.*
import org.telegram.abilitybots.api.util.AbilityUtils.getChatId
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.exceptions.TelegramApiException
import java.util.Objects.nonNull
import java.util.function.Consumer
import java.util.function.Predicate
import kotlin.coroutines.CoroutineContext


@Component
class Bot : AbilityBot {

    private val logger = LoggerFactory.getLogger(javaClass)

    @Autowired
    constructor(@Value("\${bot.token}") token: String = "", @Value("\${bot.username}") username: String = "") :
            super(token, username)

    @Value("\${bot.creatorId}")
    var creatorId: Int = 0

    override fun creatorId(): Int {
        return creatorId
    }

    fun isValueEqualsInMapEntry(mapName: String, key: String, comparableValue: String): Boolean {
        val value = db.getMap<Any, Any>(mapName)[key].toString()
        return nonNull(value) && comparableValue.equals(value)
    }

    fun rewriteValueInMapEntry(mapName: String, key: String, value: String) {
        db.getMap<Any, Any>(mapName)[key] = value
    }

    @Autowired
    lateinit var startCommandHandler: ICommandHandler

    @Autowired
    lateinit var addBookCommandHandler: ICommandHandler

    @Autowired
    lateinit var getAllBooksCommandHandler: ICommandHandler

    @Autowired
    lateinit var removeBookCommandHandler: ICommandHandler

    //TODO: Delete from here
    @Autowired
    lateinit var scheduler: Scheduler

    //TODO: Delete from here
    @Autowired
    lateinit var userService: IUserService

    //TODO: Delete from here
    @Autowired
    lateinit var bookPartSender: BookPartSender

    //TODO: Delete from here
    @Autowired
    lateinit var googleDrive: Drive

    @Autowired
    lateinit var cc: CoroutineContext

    fun String?.makeBold(): String {
        return "*$this*"
    }

    fun String?.makeItalic(): String {
        return "_${this}_"
    }

    fun onStart(): Ability {
        return Ability.builder()
                .name("start")
                .info("start")
                .input(0)
                .privacy(Privacy.PUBLIC)
                .locality(Locality.USER)
                .action { startCommandHandler.answer(it.update()) }
                .post {}
                .build()
    }

    fun addBook(): Ability {
        return Ability.builder()
                .name("addbook")
                .info("Adding new book")
                .input(0)
                .privacy(Privacy.PUBLIC)
                .locality(Locality.USER)
                .action { addBookCommandHandler.answer(it.update()) }
                .post {}
                .build()
    }

    fun replyToBook(): Reply {
        val action =
                Consumer { update: Update ->
                    //If user profile waiting for book
                    if (isValueEqualsInMapEntry(BotConstants.CHAT_STATES,
                                    getChatId(update).toString(),
                                    States.WAIT_FOR_BOOK.toString()))
                        addBookCommandHandler.firstReply(update)
                }
        return Reply.of(action, Flag.DOCUMENT);
    }


    fun cancelSending(): Reply {
        val action = Consumer { update: Update ->
            try {
                if (isValueEqualsInMapEntry(BotConstants.CHAT_STATES,
                                getChatId(update).toString(),
                                States.WAIT_FOR_BOOK.toString())) {
                    rewriteValueInMapEntry(BotConstants.CHAT_STATES,
                            getChatId(update).toString(),
                            States.NOT_WAITING.toString())
                    this.execute(SendMessage()
                            .setText("Action canceled.".makeItalic())
                            .enableMarkdown(true)
                            .setChatId(getChatId(update))
                            .setReplyMarkup(KeyboardFactory.removeKeyboard()))
                }

            } catch (e: TelegramApiException) {
                e.printStackTrace()
            }
        }
        return Reply.of(action, Flag.TEXT, Predicate { update -> update.message.text.equals("Cancel") })
    }


    fun removeBook(): Ability {
        return Ability.builder()
                .name("removebook")
                .info("Remove book from list")
                .input(0)
                .privacy(Privacy.PUBLIC)
                .locality(Locality.USER)
                .action { removeBookCommandHandler.answer(it.update()) }
                .post {}
                .build()
    }

    fun replyToRemoveCallback(): Reply? {
        val action = Consumer { update: Update ->
            if (isValueEqualsInMapEntry(BotConstants.CHAT_STATES,
                            getChatId(update).toString(),
                            States.WAIT_FOR_REMOVE.toString()))
                removeBookCommandHandler.firstReply(update)
        }
        return Reply.of(action, Flag.CALLBACK_QUERY)
    }

    fun replyToGetNextCallback(): Reply? {
        //TODO: Move to another class(refactor)
        val action = Consumer { update: Update ->
            userService.getUserByIdAndSubscriber(update.callbackQuery.from.id.toString(), Subscribers.TELEGRAM).subscribe(
                    Consumer { user ->
                        val callback = update.callbackQuery.data.subSequence(8, update.callbackQuery.data.length)
                        if (user.fileList.asSequence().filter { it.stillReading }.any { it.checksum == callback }) {
                            val fileinfo = user.fileList.asSequence()
                                    .first { fileInfo -> fileInfo.checksum.equals(callback) }
                            bookPartSender.sendMessageAndUpdate(fileinfo, getChatId(update).toString())
                        }
                        userService.saveUser(user).subscribe()
                    },
                    Consumer { e -> logger.error(e.message) }
            )
        }
        return Reply.of(action, Flag.CALLBACK_QUERY, Predicate { update -> update.callbackQuery.data.startsWith("getnext:") })
    }

    fun getAllBooks(): Ability {
        return Ability.builder()
                .name("mybooks")
                .info("Get list of all my books")
                .input(0)
                .privacy(Privacy.PUBLIC)
                .locality(Locality.USER)
                .action { getAllBooksCommandHandler.answer(it.update()) }
                .post {}
                .build()
    }

    fun test(): Ability {
        return Ability.builder()
                .name("test")
                .info("test")
                .input(0)
                .privacy(Privacy.ADMIN)
                .locality(Locality.USER)
                .action { scheduler.scheduler() }
                .post {}
                .build()
    }

    fun google(): Ability {
        return Ability.builder()
                .name("google")
                .info("test")
                .input(0)
                .privacy(Privacy.ADMIN)
                .locality(Locality.USER)
                .action {
                    googleDrive.files().create(File())
                }
                .post {}
                .build()
    }
}
