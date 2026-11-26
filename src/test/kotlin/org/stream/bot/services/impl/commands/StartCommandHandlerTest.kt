package org.stream.bot.services.impl.commands

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.ArgumentCaptor
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.stream.bot.TestFixtures
import org.stream.bot.captureK
import org.stream.bot.entities.Chat
import org.stream.bot.services.IChatService
import org.stream.bot.utils.ChatType
import org.stream.bot.utils.Subscribers

class StartCommandHandlerTest {

    private val bot = mockClient()
    private val chatService = mock(IChatService::class.java)
    private val handler = StartCommandHandler().also {
        it.telegramClient = bot
        it.chatService = chatService
        it.botUsername = "ReadWithMeBot"
    }

    @Test
    @ExperimentalStdlibApi
    fun `registers private chat and greets user`() {

        handler.answer(TestFixtures.textUpdate("/start"))

        val captor: ArgumentCaptor<Chat> = ArgumentCaptor.forClass(Chat::class.java)
        verify(chatService).saveUserIfNotExist(captor.captureK())
        val saved = captor.value
        assertEquals(TestFixtures.CHAT_ID_STR, saved.id)
        assertEquals(Subscribers.TELEGRAM, saved.subscriber)
        assertEquals("John", saved.firstName)
        assertEquals("Doe", saved.lastName)
        assertEquals("johndoe", saved.username)
        assertEquals(ChatType.USER, saved.chatType)

        val message = lastSentMessage(bot)
        assertEquals(TestFixtures.CHAT_ID_STR, message.chatId)
        assertEquals(msg("start.command.answer", "John", "ReadWithMeBot"), message.text)
    }

    @Test
    @ExperimentalStdlibApi
    fun `registers group chat by its title`() {

        handler.answer(TestFixtures.textUpdate("/start", chat = TestFixtures.groupChat()))

        val captor: ArgumentCaptor<Chat> = ArgumentCaptor.forClass(Chat::class.java)
        verify(chatService).saveUserIfNotExist(captor.captureK())
        assertEquals(TestFixtures.GROUP_ID.toString(), captor.value.id)
        assertEquals("Book club", captor.value.firstName)
        assertEquals(ChatType.GROUP, captor.value.chatType)
        assertEquals(TestFixtures.GROUP_ID.toString(), lastSentMessage(bot).chatId)
    }
}
