package org.stream.bot

import jakarta.annotation.PostConstruct
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.stream.bot.services.ChatStateStore
import org.stream.bot.services.ICommandHandler
import org.stream.bot.services.impl.commands.BookPartSenderService
import org.stream.bot.utils.States
import org.telegram.telegrambots.abilitybots.api.bot.AbilityBot
import org.telegram.telegrambots.abilitybots.api.db.DBContext
import org.telegram.telegrambots.abilitybots.api.objects.Ability
import org.telegram.telegrambots.abilitybots.api.objects.Flag
import org.telegram.telegrambots.abilitybots.api.objects.Locality
import org.telegram.telegrambots.abilitybots.api.objects.MessageContext
import org.telegram.telegrambots.abilitybots.api.objects.Privacy
import org.telegram.telegrambots.abilitybots.api.objects.Reply
import org.telegram.telegrambots.abilitybots.api.util.AbilityUtils
import org.telegram.telegrambots.abilitybots.api.util.AbilityUtils.getChatId
import org.telegram.telegrambots.longpolling.interfaces.LongPollingUpdateConsumer
import org.telegram.telegrambots.longpolling.starter.SpringLongPollingBot
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.generics.TelegramClient
import java.util.function.Predicate

/**
 * Telegram entry point: declares the bot commands (abilities) and replies and routes them to handlers.
 * Registered for long polling by telegrambots-springboot-longpolling-starter via [SpringLongPollingBot].
 */
@Component("bot")
class Bot(
        telegramClient: TelegramClient,
        db: DBContext,
        @Value("\${bot.token}") private val token: String,
        @Value("\${bot.username}") username: String,
        @Value("\${bot.creatorId}") private val creatorId: Long,
        private val chatStates: ChatStateStore,
        @Qualifier("startCommandHandler") private val startCommandHandler: ICommandHandler,
        @Qualifier("addBookCommandHandler") private val addBookCommandHandler: ICommandHandler,
        @Qualifier("getAllBooksCommandHandler") private val getAllBooksCommandHandler: ICommandHandler,
        @Qualifier("removeBookCommandHandler") private val removeBookCommandHandler: ICommandHandler,
        @Qualifier("cancelCommandHandler") private val cancelCommandHandler: ICommandHandler,
        private val bookPartSenderService: BookPartSenderService
) : AbilityBot(telegramClient, username, db), SpringLongPollingBot {

    /** Since telegrambots 7 abilities have to be registered explicitly. */
    @PostConstruct
    fun registerAbilities() = onRegister()

    override fun creatorId(): Long = creatorId

    override fun getBotToken(): String = token

    override fun getUpdatesConsumer(): LongPollingUpdateConsumer = this

    private fun publicAbility(name: String, info: String, locality: Locality = Locality.ALL,
                              action: (MessageContext) -> Unit): Ability =
            Ability.builder()
                    .name(name)
                    .info(info)
                    .input(0)
                    .privacy(Privacy.PUBLIC)
                    .locality(locality)
                    .action(action)
                    .build()

    private fun sendLocalized(ctx: MessageContext, key: String) {
        telegramClient.execute(SendMessage.builder()
                .chatId(ctx.chatId().toString())
                .text(AbilityUtils.getLocalizedMessage(key, ctx.user().languageCode))
                .build())
    }

    fun onStart(): Ability = publicAbility("start", "start", Locality.USER) { startCommandHandler.answer(it.update()) }

    fun addBook(): Ability = publicAbility("addbook", "Adding new book") { addBookCommandHandler.answer(it.update()) }

    fun replyToBook(): Reply = Reply.of({ _, update ->
        //If user profile waiting for book
        if (chatStates.isInState(getChatId(update).toString(), States.WAIT_FOR_BOOK))
            addBookCommandHandler.firstReply(update)
    }, Flag.DOCUMENT, Flag.MESSAGE)

    fun cancelSending(): Reply = Reply.of({ _, update -> cancelCommandHandler.answer(update) },
            Flag.TEXT,
            Predicate { update -> update.message.text == AbilityUtils.getLocalizedMessage("cancel",
                    AbilityUtils.getUser(update).languageCode) })

    fun removeBook(): Ability = publicAbility("removebook", "Remove book from list") { removeBookCommandHandler.answer(it.update()) }

    fun replyToRemoveCallback(): Reply = Reply.of({ _, update ->
        if (chatStates.isInState(getChatId(update).toString(), States.WAIT_FOR_REMOVE))
            removeBookCommandHandler.firstReply(update)
    }, Flag.CALLBACK_QUERY)

    fun replyToGetNextBookPart(): Reply = Reply.of({ _, update -> bookPartSenderService.replyOnNextPageCallback(update) },
            Flag.CALLBACK_QUERY,
            Predicate { update -> update.callbackQuery.data.startsWith("getnext:") })

    fun getAllBooks(): Ability = publicAbility("mybooks", "Get list of all my books") { getAllBooksCommandHandler.answer(it.update()) }

    fun about(): Ability = publicAbility("about", "Bot description") { sendLocalized(it, "bot.description") }

    fun help(): Ability = publicAbility("help", "Bot commands description") { sendLocalized(it, "bot.help") }

    fun feedback(): Ability = publicAbility("feedback", "Information on how to leave feedback") { sendLocalized(it, "bot.feedback") }

    fun groupStart(): Reply {
        val isStartInGroup = Predicate<Update> {
            AbilityUtils.isGroupUpdate(it) &&
                    (it.message.newChatMembers.orEmpty().any { member -> member.userName.equals(botUsername, ignoreCase = true) }
                            || it.message.groupchatCreated == true)
        }
        return Reply.of({ _, update -> startCommandHandler.answer(update) }, Flag.MESSAGE, isStartInGroup)
    }
}
