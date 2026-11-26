package org.stream.bot.services

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.mapdb.DBMaker
import org.stream.bot.utils.States
import org.telegram.telegrambots.abilitybots.api.db.MapDBContext

class ChatStateStoreTest {

    private val store = ChatStateStore(MapDBContext(DBMaker.memoryDB().make()))

    @Test
    fun `unknown chat is in no state`() {
        States.values().forEach { assertFalse(store.isInState("1", it)) }
    }

    @Test
    fun `remembers last state per chat`() {
        store.setState("1", States.WAIT_FOR_BOOK)
        store.setState("2", States.WAIT_FOR_REMOVE)
        store.setState("1", States.NOT_WAITING)

        assertTrue(store.isInState("1", States.NOT_WAITING))
        assertFalse(store.isInState("1", States.WAIT_FOR_BOOK))
        assertTrue(store.isInState("2", States.WAIT_FOR_REMOVE))
    }
}
