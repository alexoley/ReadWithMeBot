package org.stream.bot

import com.google.api.services.drive.Drive
import com.google.api.services.drive.model.File
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.stream.bot.services.ICommandHandler
import org.stream.bot.services.IUserService
import org.stream.bot.services.MARKDOWN_ENABLED
import org.stream.bot.services.impl.commands.BookPartSenderService
import org.stream.bot.services.impl.Scheduler
import org.stream.bot.services.impl.commands.CancelCommandHandler
import org.stream.bot.utils.BotConstants
import org.stream.bot.utils.KeyboardFactory
import org.stream.bot.utils.States
import org.telegram.abilitybots.api.bot.AbilityBot
import org.telegram.abilitybots.api.objects.*
import org.telegram.abilitybots.api.util.AbilityUtils
import org.telegram.abilitybots.api.util.AbilityUtils.getChatId
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.exceptions.TelegramApiException
import java.util.Objects.nonNull
import java.util.function.Consumer
import java.util.function.Predicate
import kotlin.coroutines.CoroutineContext


@Component("bot")
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

    @Autowired
    lateinit var cancelCommandHandler: ICommandHandler

    @Autowired
    lateinit var bookPartSenderService: BookPartSenderService

    //TODO: Migrate to coroutines
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
                .locality(Locality.ALL)
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
        val action = Consumer { update: Update -> cancelCommandHandler.answer(update) }
        return Reply.of(action, Flag.TEXT, Predicate { update -> update.message.text.equals(AbilityUtils.getLocalizedMessage("cancel",
                AbilityUtils.getUser(update).languageCode)) })
    }


    fun removeBook(): Ability {
        return Ability.builder()
                .name("removebook")
                .info("Remove book from list")
                .input(0)
                .privacy(Privacy.PUBLIC)
                .locality(Locality.ALL)
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

    fun replyToGetNextBookPart(): Reply {
        val action = Consumer { update: Update -> bookPartSenderService.replyOnNextPageCallback(update) }
        return Reply.of(action, Flag.CALLBACK_QUERY,
                Predicate { update -> update.callbackQuery.data.startsWith("getnext:") })
    }

    fun getAllBooks(): Ability {
        return Ability.builder()
                .name("mybooks")
                .info("Get list of all my books")
                .input(0)
                .privacy(Privacy.PUBLIC)
                .locality(Locality.ALL)
                .action { getAllBooksCommandHandler.answer(it.update()) }
                .post {}
                .build()
    }

    fun about(): Ability {
        return Ability.builder()
                .name("about")
                .info("Bot description")
                .input(0)
                .privacy(Privacy.PUBLIC)
                .locality(Locality.ALL)
                .action { this.execute(SendMessage()
                        .setChatId(it.chatId())
                        .setText(AbilityUtils.getLocalizedMessage("bot.description", it.user().languageCode)))}
                .post {}
                .build()
    }

    fun help(): Ability {
        return Ability.builder()
                .name("help")
                .info("Bot commands description")
                .input(0)
                .privacy(Privacy.PUBLIC)
                .locality(Locality.ALL)
                .action { this.execute(SendMessage()
                        .setChatId(it.chatId())
                        .setText(AbilityUtils.getLocalizedMessage("bot.help", it.user().languageCode)))}
                .post {}
                .build()
    }

    fun groupStart(): Reply {
        val isStartInGroup = Predicate<Update> { AbilityUtils.isGroupUpdate(it) &&
                ((it.message.newChatMembers.isNullOrEmpty().not() && it.message.newChatMembers[0].id==me.id)
                        || (it.message.groupchatCreated!=null && it.message.groupchatCreated))}
        val action = Consumer { update: Update -> startCommandHandler.answer(update) }
        return Reply.of(action, Flag.MESSAGE, isStartInGroup)
    }
}
