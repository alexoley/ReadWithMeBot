package org.stream.bot

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.stereotype.Component
import org.stream.bot.entities.User
import org.stream.bot.services.ICommandHandler
import org.stream.bot.services.IUserService
import org.stream.bot.utils.BotConstants
import org.stream.bot.utils.KeyboardFactory
import org.stream.bot.utils.States
import org.stream.bot.utils.Subscribers
import org.telegram.abilitybots.api.bot.AbilityBot
import org.telegram.abilitybots.api.db.DBContext
import org.telegram.abilitybots.api.objects.*
import org.telegram.abilitybots.api.sender.MessageSender
import org.telegram.abilitybots.api.util.AbilityUtils.getChatId
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.exceptions.TelegramApiException
import java.util.Objects.nonNull
import java.util.function.Consumer
import java.util.function.Predicate

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
    lateinit var addBookCommandHandler: ICommandHandler

    @Autowired
    lateinit var userService: IUserService


    fun onStart(): Ability {
        return Ability.builder()
                .name("start")
                .info("start")
                .input(0)
                .privacy(Privacy.PUBLIC)
                .locality(Locality.USER)
                .action { ctx: MessageContext ->
                    userService.saveUserIfNotExist(User(id = ctx.user().id.toString(),
                            subscriber = Subscribers.TELEGRAM,
                            firstName = ctx.user().firstName,
                            lastName = ctx.user().lastName,
                            nickname = ctx.user().userName))
                    this.execute(SendMessage().setText("It’s nice to meet you, " + ctx.user().firstName + "!\n" +
                            "Welcome to ReadWithMe Bot. I'm here to help you read books.\n" +
                            "I'll send you the part of book each day, that you'd like to read.")
                            .setChatId(ctx.chatId()))
                    //TODO: Good morning/Good afternoon/Good evening
                }
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
                .action { ctx: MessageContext ->
                    try {
                        this.execute(SendMessage()
                                .setText("Send me book. The book should be no more than 20 megabytes.")
                                .setChatId(ctx.chatId())
                                .setReplyMarkup(KeyboardFactory.cancelButton()))
                        //db.getMap<Any, Any>(BotConstants.CHAT_STATES)[ctx.chatId().toString()] = States.WAIT_FOR_BOOK
                        rewriteValueInMapEntry(BotConstants.CHAT_STATES, ctx.chatId().toString(), States.WAIT_FOR_BOOK.toString())
                    } catch (e: TelegramApiException) {
                        e.printStackTrace()
                    }
                }
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
                    addBookCommandHandler.process(update)
                }
        return Reply.of(action, Flag.DOCUMENT)

    }

    fun cancelSending(): Reply {
        val action = Consumer { update: Update ->
            try {
                if (isValueEqualsInMapEntry(BotConstants.CHAT_STATES,
                                getChatId(update).toString(),
                                States.WAIT_FOR_BOOK.toString())) {
                    rewriteValueInMapEntry(BotConstants.CHAT_STATES,
                            getChatId(update).toString(),
                            States.NOT_WAITING_FOR_BOOK.toString())
                    this.execute(SendMessage()
                            .setText("Action canceled.")
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
                .action { ctx: MessageContext ->
                    this.execute(SendMessage()
                            .setText("Not supported yet")
                            .setChatId(ctx.chatId()))
                }
                .post {}
                .build()
    }

    fun getAllBooks(): Ability {
        return Ability.builder()
                .name("mybooks")
                .info("Get list of all my books")
                .input(0)
                .privacy(Privacy.PUBLIC)
                .locality(Locality.USER)
                .action { ctx: MessageContext ->
                    this.execute(SendMessage()
                            .setText("Not supported yet")
                            .setChatId(ctx.chatId()))
                }
                .post {}
                .build()
    }
}
