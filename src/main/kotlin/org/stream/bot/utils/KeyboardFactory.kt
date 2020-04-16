package org.stream.bot.utils

import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow


class KeyboardFactory {
    companion object {

        fun removeKeyboard(): ReplyKeyboard {
            return ReplyKeyboardRemove()
        }

        fun cancelButton(): ReplyKeyboard {
            val keyboardRow = KeyboardRow();
            keyboardRow.add("Cancel")
            val keyboardRowList = listOf(keyboardRow)
            val replyKeyboardMarkup = ReplyKeyboardMarkup()
            replyKeyboardMarkup
                    .setKeyboard(keyboardRowList)
                    .setOneTimeKeyboard(true)
                    .setResizeKeyboard(true)
            return replyKeyboardMarkup;
        }
    }
}