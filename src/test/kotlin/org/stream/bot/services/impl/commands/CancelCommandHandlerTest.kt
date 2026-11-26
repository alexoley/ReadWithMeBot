package org.stream.bot.services.impl.commands

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.stream.bot.TestFixtures
import org.stream.bot.anyK
import org.stream.bot.utils.BotConstants
import org.stream.bot.utils.States
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove

class CancelCommandHandlerTest {

    private val bot = mockClient()
    private val states = mockStates()
    private val handler = CancelCommandHandler().also { it.telegramClient = bot
        it.chatStates = states }

    private fun givenWaitingForBook(waiting: Boolean) {
        `when`(states.isInState(TestFixtures.CHAT_ID_STR, States.WAIT_FOR_BOOK))
                .thenReturn(waiting)
    }

    @Test
    fun `cancels waiting for book`() {
        givenWaitingForBook(true)

        handler.answer(TestFixtures.textUpdate("Cancel"))

        verify(states).setState(TestFixtures.CHAT_ID_STR, States.NOT_WAITING)
        val message = lastSentMessage(bot)
        assertEquals(msg("cancel.command.action.canceled"), message.text)
        assertTrue(message.replyMarkup is ReplyKeyboardRemove)
    }

    @Test
    fun `does nothing when not waiting`() {
        givenWaitingForBook(false)

        handler.answer(TestFixtures.textUpdate("Cancel"))

        verify(states, never()).setState(anyString(), anyK(States::class.java))
        assertTrue(org.mockito.Mockito.mockingDetails(bot).invocations.none { it.method.name == "execute" })
    }
}
