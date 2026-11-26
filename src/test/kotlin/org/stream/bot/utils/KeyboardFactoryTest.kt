package org.stream.bot.utils

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.stream.bot.TestFixtures
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove

class KeyboardFactoryTest {

    @Test
    fun `remove keyboard removes it`() {
        val keyboard = KeyboardFactory.removeKeyboard() as ReplyKeyboardRemove
        assertTrue(keyboard.removeKeyboard)
    }

    @Test
    fun `cancel button is a single one-time button`() {
        val keyboard = KeyboardFactory.cancelButton("Cancel") as ReplyKeyboardMarkup
        assertEquals(listOf("Cancel"), keyboard.keyboard.single().map { it.text })
        assertTrue(keyboard.oneTimeKeyboard)
        assertTrue(keyboard.resizeKeyboard)
    }

    @Test
    fun `delete keyboard has one row per book with checksum callback`() {
        val books = listOf(TestFixtures.fileInfo("a.pdf", "c1"), TestFixtures.fileInfo("b.pdf", "c2"))

        val keyboard = KeyboardFactory.inlineBookDeleteKeyboardFromList(books) as InlineKeyboardMarkup

        assertEquals(listOf("a.pdf" to "c1", "b.pdf" to "c2"),
                keyboard.keyboard.map { it.single().text to it.single().callbackData })
    }

    @Test
    fun `give-more button shortens long names and fits telegram callback limit`() {
        val book = TestFixtures.fileInfo("A very long book name.pdf", "0123456789abcdef0123456789abcdef")

        val button = (KeyboardFactory.inlineBookGiveMoreKeyboard(book) as InlineKeyboardMarkup).keyboard.single().single()

        assertEquals("A very lon...👉", button.text)
        assertEquals("getnext:0123456789abcdef0123456789abcdef", button.callbackData)
        assertTrue(button.callbackData.toByteArray().size <= 64)
    }

    @Test
    fun `give-more button keeps short names`() {
        val button = (KeyboardFactory.inlineBookGiveMoreKeyboard(TestFixtures.fileInfo("short.txt")) as InlineKeyboardMarkup)
                .keyboard.single().single()
        assertEquals("short.txt...👉", button.text)
    }
}
