package org.stream.bot.services

import org.springframework.stereotype.Component
import org.stream.bot.utils.BotConstants
import org.stream.bot.utils.States
import org.telegram.telegrambots.abilitybots.api.db.DBContext

/** Remembers what the bot is waiting for in each chat (see [States]). */
@Component
class ChatStateStore(private val db: DBContext) {

    private fun states(): MutableMap<String, String> = db.getMap(BotConstants.CHAT_STATES)

    fun isInState(chatId: String, state: States): Boolean = states()[chatId] == state.toString()

    fun setState(chatId: String, state: States) {
        states()[chatId] = state.toString()
    }
}
