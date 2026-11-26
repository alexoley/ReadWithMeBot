package org.stream.bot.services.impl.commands

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.stream.bot.TestFixtures
import org.stream.bot.entities.Chat
import org.stream.bot.services.IChatService
import org.stream.bot.services.impl.TextPartExtractorService
import org.stream.bot.utils.Subscribers
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup
import reactor.core.publisher.Mono

class BookPartSenderServiceTest {

    private val bot = mockClient()
    private val chatService = mock(IChatService::class.java)
    private val extractor = mock(TextPartExtractorService::class.java)
    private val service = BookPartSenderService()

    @BeforeEach
    fun setUp() {
        service.telegramClient = bot
        service.chatService = chatService
        service.textPartExtractorService = extractor
    }

    private fun givenChat(chat: Chat) {
        `when`(chatService.getUserByIdAndSubscriber(TestFixtures.CHAT_ID_STR, Subscribers.TELEGRAM))
                .thenReturn(Mono.just(chat))
        `when`(chatService.saveUser(chat)).thenReturn(Mono.just(chat))
    }

    @Test
    fun `sends next part with give-more button`() {
        val book = TestFixtures.fileInfo(checksum = "abc")
        `when`(extractor.getNextTextPartAndPosition(book)).thenReturn("book.txt  page 1\n\nText.")

        service.sendMessageAndUpdateFileInfo(book, TestFixtures.CHAT_ID_STR)

        val message = lastSentMessage(bot)
        assertEquals("book.txt  page 1\n\nText.", message.text)
        assertEquals(TestFixtures.CHAT_ID_STR, message.chatId)
        assertEquals(true, message.disableWebPagePreview)
        val button = (message.replyMarkup as InlineKeyboardMarkup).keyboard.single().single()
        assertEquals("getnext:abc", button.callbackData)
    }

    @Test
    fun `next page callback sends the book and saves progress`() {
        val book = TestFixtures.fileInfo(checksum = "abc")
        val chat = TestFixtures.chat(book)
        givenChat(chat)
        `when`(extractor.getNextTextPartAndPosition(book)).thenReturn("next part")

        service.replyOnNextPageCallback(TestFixtures.callbackUpdate("getnext:ABC"))

        assertEquals("next part", lastSentMessage(bot).text)
        verify(chatService).saveUser(chat)
    }

    @Test
    fun `next page callback ignores finished book`() {
        val book = TestFixtures.fileInfo(checksum = "abc", stillReading = false)
        val chat = TestFixtures.chat(book)
        givenChat(chat)

        service.replyOnNextPageCallback(TestFixtures.callbackUpdate("getnext:abc"))

        verify(extractor, never()).getNextTextPartAndPosition(book)
        verify(chatService, never()).saveUser(chat)
    }
}
