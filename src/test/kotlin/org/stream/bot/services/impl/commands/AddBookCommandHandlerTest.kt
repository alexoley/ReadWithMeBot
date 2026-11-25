package org.stream.bot.services.impl.commands

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.stream.bot.anyK
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mockito.mock
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.mockito.Mockito.verifyNoInteractions
import org.mockito.Mockito.`when`
import org.stream.bot.TestFixtures
import org.stream.bot.entities.Chat
import org.stream.bot.entities.FileInfo
import org.stream.bot.exceptions.DublicateBookException
import org.stream.bot.services.IChatService
import org.stream.bot.services.IDocumentPersistManager
import org.stream.bot.services.impl.document.extractors.TextFormatExtractor
import org.stream.bot.utils.BotConstants
import org.stream.bot.utils.States
import org.stream.bot.utils.Subscribers
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ForceReplyKeyboard
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove
import reactor.core.publisher.Mono

class AddBookCommandHandlerTest {

    private val bot = mockBot()
    private val chatService = mock(IChatService::class.java)
    private val persistManager = mock(IDocumentPersistManager::class.java)
    private val sender = mock(BookPartSenderService::class.java)
    private val handler = AddBookCommandHandler()

    @BeforeEach
    fun setUp() {
        handler.bot = bot
        handler.chatService = chatService
        handler.documentPersistManager = persistManager
        handler.bookPartSenderService = sender
        handler.documentFormatExtractorList = listOf(TextFormatExtractor())
    }

    private fun givenChat(chat: Chat) {
        `when`(chatService.getUserByIdAndSubscriber(TestFixtures.CHAT_ID_STR, Subscribers.TELEGRAM))
                .thenReturn(Mono.just(chat))
        `when`(chatService.saveUser(chat)).thenReturn(Mono.just(chat))
    }

    // ---------- /addbook ----------

    @Test
    fun `asks for a book and waits for it when limit not reached`() {
        givenChat(TestFixtures.chat(limit = 2))

        handler.answer(TestFixtures.textUpdate("/addbook"))

        val message = lastSentMessage(bot)
        assertEquals(msg("addbook.command.send.me.book"), message.text)
        assertTrue(message.replyMarkup is ForceReplyKeyboard)
        verify(bot).rewriteValueInMapEntry(BotConstants.CHAT_STATES, TestFixtures.CHAT_ID_STR, States.WAIT_FOR_BOOK.toString())
    }

    @Test
    fun `refuses when book limit reached`() {
        givenChat(TestFixtures.chat(TestFixtures.fileInfo(checksum = "a"), TestFixtures.fileInfo(checksum = "b"), limit = 2))

        handler.answer(TestFixtures.textUpdate("/addbook"))

        assertEquals(msg("addbook.command.book.limit.exceeded", 2), lastSentMessage(bot).text)
        verify(bot, never()).rewriteValueInMapEntry(anyString(), anyString(), anyString())
    }

    @Test
    fun `tells unknown chat to run start`() {
        `when`(chatService.getUserByIdAndSubscriber(TestFixtures.CHAT_ID_STR, Subscribers.TELEGRAM))
                .thenReturn(Mono.empty())

        handler.answer(TestFixtures.textUpdate("/addbook"))

        assertEquals(msg("addbook.command.chat.not.found"), lastSentMessage(bot).text)
    }

    @Test
    fun `reports server error when database fails`() {
        `when`(chatService.getUserByIdAndSubscriber(TestFixtures.CHAT_ID_STR, Subscribers.TELEGRAM))
                .thenReturn(Mono.error(IllegalStateException("db down")))

        handler.answer(TestFixtures.textUpdate("/addbook"))

        assertEquals(msg("error.message.something.wrong.on.server"), lastSentMessage(bot).text)
    }

    // ---------- document received ----------

    private fun persistReturns(answer: (Update) -> FileInfo) {
        `when`(persistManager.persistToStorage(anyK(Update::class.java), anyString(), anyK()))
                .thenAnswer { answer(it.getArgument(0)) }
    }

    @Test
    fun `stores supported book, sends first part and saves chat`() {
        val chat = TestFixtures.chat()
        givenChat(chat)
        val stored = TestFixtures.fileInfo(fileName = "book.txt")
        persistReturns { stored }

        handler.firstReply(TestFixtures.documentUpdate(TestFixtures.document(fileName = "book.txt")))

        assertEquals(listOf(stored), chat.fileList)
        val message = lastSentMessage(bot)
        assertEquals(msg("addbook.command.successful.book.addition", "book.txt"), message.text)
        assertTrue(message.replyMarkup is ReplyKeyboardRemove)
        verify(bot).rewriteValueInMapEntry(BotConstants.CHAT_STATES, TestFixtures.CHAT_ID_STR, States.NOT_WAITING.toString())
        verify(sender).sendMessageAndUpdateFileInfo(stored, TestFixtures.CHAT_ID_STR)
        verify(chatService).saveUser(chat)
    }

    @Test
    fun `sends loading message for files over one megabyte`() {
        givenChat(TestFixtures.chat())
        persistReturns { TestFixtures.fileInfo() }

        handler.firstReply(TestFixtures.documentUpdate(TestFixtures.document(fileSize = 2 * 1024 * 1024)))

        val texts = sentMessages(bot).map { it.text }
        assertEquals(listOf(msg("addbook.command.loading.file"),
                msg("addbook.command.successful.book.addition", "book.txt")), texts)
    }

    @Test
    fun `rejects too large book`() {
        val chat = TestFixtures.chat()
        givenChat(chat)

        handler.firstReply(TestFixtures.documentUpdate(
                TestFixtures.document(fileSize = BotConstants.MAX_TELEGRAM_FILE_SIZE + 1)))

        assertEquals(msg("addbook.command.too.large.book"), lastSentMessage(bot).text)
        verifyNoInteractions(persistManager, sender)
        assertTrue(chat.fileList.isEmpty())
    }

    @Test
    fun `rejects unsupported format`() {
        givenChat(TestFixtures.chat())

        handler.firstReply(TestFixtures.documentUpdate(TestFixtures.document(fileName = "a.doc", mimeType = "application/msword")))

        assertEquals(msg("addbook.command.not.support.document.format"), lastSentMessage(bot).text)
        verifyNoInteractions(persistManager, sender)
    }

    @Test
    fun `rejects duplicate book`() {
        val chat = TestFixtures.chat()
        givenChat(chat)
        persistReturns { throw DublicateBookException() }

        handler.firstReply(TestFixtures.documentUpdate())

        assertEquals(msg("addbook.command.dublicate.book"), lastSentMessage(bot).text)
        verifyNoInteractions(sender)
        assertTrue(chat.fileList.isEmpty())
    }
}
