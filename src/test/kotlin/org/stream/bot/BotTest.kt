package org.stream.bot

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mapdb.DBMaker
import org.mockito.Mockito.mock
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.stream.bot.services.ChatStateStore
import org.stream.bot.services.ICommandHandler
import org.stream.bot.services.impl.commands.BookPartSenderService
import org.stream.bot.utils.States
import org.telegram.telegrambots.abilitybots.api.db.MapDBContext
import org.telegram.telegrambots.meta.api.objects.User
import org.telegram.telegrambots.meta.generics.TelegramClient

class BotTest {

    private val db = MapDBContext(DBMaker.memoryDB().make())
    private val states = ChatStateStore(db)
    private val start = mock(ICommandHandler::class.java)
    private val addBook = mock(ICommandHandler::class.java)
    private val getAll = mock(ICommandHandler::class.java)
    private val remove = mock(ICommandHandler::class.java)
    private val cancel = mock(ICommandHandler::class.java)
    private val sender = mock(BookPartSenderService::class.java)
    private lateinit var bot: Bot

    @BeforeEach
    fun setUp() {
        bot = Bot(mock(TelegramClient::class.java), db, "123:token", "ReadWithMeBot", 42L,
                states, start, addBook, getAll, remove, cancel, sender)
        bot.registerAbilities()
    }

    @Test
    fun `exposes token, creator and itself as update consumer`() {
        assertEquals("123:token", bot.botToken)
        assertEquals(42L, bot.creatorId())
        assertSame(bot, bot.updatesConsumer)
    }

    @Test
    fun `registers all user commands`() {
        assertTrue(bot.abilities.keys.containsAll(listOf("start", "addbook", "mybooks", "removebook", "about", "help", "feedback")),
                bot.abilities.keys.toString())
    }

    @Test
    fun `document is handled only while waiting for a book`() {
        val update = TestFixtures.documentUpdate()

        bot.replyToBook().actOn(bot, update)
        verify(addBook, never()).firstReply(update)

        states.setState(TestFixtures.CHAT_ID_STR, States.WAIT_FOR_BOOK)
        bot.replyToBook().actOn(bot, update)
        verify(addBook).firstReply(update)
    }

    @Test
    fun `remove callback is handled only while waiting for removal`() {
        val update = TestFixtures.callbackUpdate("checksum")

        bot.replyToRemoveCallback().actOn(bot, update)
        verify(remove, never()).firstReply(update)

        states.setState(TestFixtures.CHAT_ID_STR, States.WAIT_FOR_REMOVE)
        bot.replyToRemoveCallback().actOn(bot, update)
        verify(remove).firstReply(update)
    }

    @Test
    fun `get-next callback goes to book sender`() {
        val next = TestFixtures.callbackUpdate("getnext:abc")
        val other = TestFixtures.callbackUpdate("abc")

        assertTrue(bot.replyToGetNextBookPart().isOkFor(next))
        assertFalse(bot.replyToGetNextBookPart().isOkFor(other))
        bot.replyToGetNextBookPart().actOn(bot, next)
        verify(sender).replyOnNextPageCallback(next)
    }

    @Test
    fun `cancel reply reacts to localized cancel text only`() {
        assertTrue(bot.cancelSending().isOkFor(TestFixtures.textUpdate("Cancel")))
        assertFalse(bot.cancelSending().isOkFor(TestFixtures.textUpdate("Something else")))
    }

    @Test
    fun `group start triggers when bot is added to a group`() {
        val update = TestFixtures.textUpdate(text = null, chat = TestFixtures.groupChat())
        val botUser = User(7L, "Bot", true)
        botUser.userName = "readwithmebot"
        update.message.newChatMembers = listOf(botUser)

        assertTrue(bot.groupStart().isOkFor(update))
        bot.groupStart().actOn(bot, update)
        verify(start).answer(update)

        update.message.newChatMembers = listOf(TestFixtures.user())
        assertFalse(bot.groupStart().isOkFor(update))
    }
}
