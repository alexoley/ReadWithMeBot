package org.stream.bot.services

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.telegram.telegrambots.meta.api.objects.Update

class ICommandHandlerTest {

    private val handler = object : ICommandHandler {
        override fun answer(update: Update) {}
        override fun firstReply(update: Update) {}
        override fun secondReply(update: Update) {}
        override fun thirdReply(update: Update) {}

        fun bot(s: String?) = s.botText()
        fun book(s: String?) = s.bookText()
        fun escape(s: String?) = s.escapeCharacters()
    }

    @Test
    fun `botText never returns null`() {
        assertEquals("", handler.bot(null))
        assertEquals("hi", handler.bot("hi"))
    }

    @Test
    fun `bookText makes text bold`() {
        assertEquals("*title*", handler.book("title"))
    }

    @Test
    fun `escapes markdown characters once`() {
        assertEquals("a\\_b\\.c\\!", handler.escape("a_b.c!"))
        assertEquals("back\\\\slash", handler.escape("back\\slash"))
        assertEquals("", handler.escape(null))
    }
}
