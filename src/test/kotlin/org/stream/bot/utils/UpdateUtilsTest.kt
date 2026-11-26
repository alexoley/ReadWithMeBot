package org.stream.bot.utils

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.stream.bot.TestFixtures

class UpdateUtilsTest {

    @Test
    fun `private chat data comes from the user`() {
        val update = TestFixtures.textUpdate()
        assertEquals("John", update.getChatFirstName())
        assertEquals("Doe", update.getChatLastName())
        assertEquals("johndoe", update.getChatUsername())
        assertEquals(ChatType.USER, update.getChatType())
    }

    @Test
    fun `group chat data comes from the chat`() {
        val update = TestFixtures.textUpdate(chat = TestFixtures.groupChat())
        assertEquals("Book club", update.getChatFirstName())
        assertEquals("", update.getChatLastName())
        assertEquals("", update.getChatUsername())
        assertEquals(ChatType.GROUP, update.getChatType())
    }
}
