package org.stream.bot.services.impl.commands

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mockito.mock
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.mockito.Mockito.verifyNoInteractions
import org.mockito.Mockito.`when`
import org.stream.bot.TestFixtures
import org.stream.bot.anyK
import org.stream.bot.entities.Chat
import org.stream.bot.services.IChatService
import org.stream.bot.services.IDocumentPersistManager
import org.stream.bot.utils.BotConstants
import org.stream.bot.utils.States
import org.stream.bot.utils.Subscribers
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup
import reactor.core.publisher.Mono

class RemoveBookCommandHandlerTest {

    private val bot = mockClient()
    private val states = mockStates()
    private val chatService = mock(IChatService::class.java)
    private val persistManager = mock(IDocumentPersistManager::class.java)
    private val handler = RemoveBookCommandHandler().also {
        it.telegramClient = bot
        it.chatStates = states
        it.chatService = chatService
        it.persistManager = persistManager
    }

    private val book1 = TestFixtures.fileInfo("first.pdf", "aaa")
    private val book2 = TestFixtures.fileInfo("second.txt", "bbb")

    private fun givenChat(chat: Chat) {
        `when`(chatService.getUserByIdAndSubscriber(TestFixtures.CHAT_ID_STR, Subscribers.TELEGRAM))
                .thenReturn(Mono.just(chat))
        `when`(chatService.saveUser(chat)).thenReturn(Mono.just(chat))
    }

    @Test
    fun `says there is nothing to remove`() {
        givenChat(TestFixtures.chat())

        handler.answer(TestFixtures.textUpdate("/removebook"))

        assertEquals(msg("remove.book.command.have.no.books"), lastSentMessage(bot).text)
        verify(states, never()).setState(anyString(), anyK(States::class.java))
    }

    @Test
    fun `offers books to remove and waits for choice`() {
        givenChat(TestFixtures.chat(book1, book2))

        handler.answer(TestFixtures.textUpdate("/removebook"))

        val message = lastSentMessage(bot)
        assertEquals(msg("remove.book.command.what.to.delete"), message.text)
        val keyboard = message.replyMarkup as InlineKeyboardMarkup
        assertEquals(listOf("aaa", "bbb"), keyboard.keyboard.map { it.single().callbackData })
        verify(states).setState(TestFixtures.CHAT_ID_STR, States.WAIT_FOR_REMOVE)
    }

    @Test
    fun `removes chosen book and stops waiting`() {
        val chat = TestFixtures.chat(book1, book2)
        givenChat(chat)
        `when`(persistManager.removeFromStorage(book1)).thenReturn(true)

        handler.firstReply(TestFixtures.callbackUpdate("aaa"))

        assertEquals(listOf(book2), chat.fileList)
        verify(chatService).saveUser(chat)
        assertEquals(msg("remove.book.command.success.remove", "first.pdf"), lastSentMessage(bot).text)
        verify(states).setState(TestFixtures.CHAT_ID_STR, States.NOT_WAITING)
    }

    @Test
    fun `ignores unknown callback`() {
        val chat = TestFixtures.chat(book1)
        givenChat(chat)

        handler.firstReply(TestFixtures.callbackUpdate("getnext:aaa"))

        assertEquals(listOf(book1), chat.fileList)
        verifyNoInteractions(persistManager)
        assertTrue(org.mockito.Mockito.mockingDetails(bot).invocations.none { it.method.name == "execute" })
    }
}
