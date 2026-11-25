package org.stream.bot.services.impl.commands

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.stream.bot.TestFixtures
import org.stream.bot.entities.Chat
import org.stream.bot.services.IChatService
import org.stream.bot.utils.Subscribers
import reactor.core.publisher.Mono

class GetAllBooksCommandHandlerTest {

    private val bot = mockBot()
    private val chatService = mock(IChatService::class.java)
    private val handler = GetAllBooksCommandHandler().also {
        it.bot = bot
        it.chatService = chatService
    }

    private fun givenChat(chat: Chat) {
        `when`(chatService.getUserByIdAndSubscriber(TestFixtures.CHAT_ID_STR, Subscribers.TELEGRAM))
                .thenReturn(Mono.just(chat))
    }

    @Test
    fun `says there are no books`() {
        givenChat(TestFixtures.chat())

        handler.answer(TestFixtures.textUpdate("/mybooks"))

        assertEquals(msg("books.list.command.have.no.books"), lastSentMessage(bot).text)
    }

    @Test
    fun `lists books with numbers`() {
        givenChat(TestFixtures.chat(TestFixtures.fileInfo("first.pdf", "a"), TestFixtures.fileInfo("second.txt", "b")))

        handler.answer(TestFixtures.textUpdate("/mybooks"))

        assertEquals(msg("books.list.command.book.list") + "1. first.pdf\n2. second.txt\n", lastSentMessage(bot).text)
    }
}
