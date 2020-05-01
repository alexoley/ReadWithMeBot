package org.stream.bot.utils

import org.stream.bot.entities.FileInfo
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow
import kotlin.collections.ArrayList


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

        fun inlineBookDeleteKeyboardFromList(bookList: List<FileInfo>): ReplyKeyboard {
            val rowsInline: List<List<InlineKeyboardButton>> =
            bookList.map { fileInfo -> InlineKeyboardButton().setText(fileInfo.fileName).setCallbackData(fileInfo.checksum) }
                    .map { inlineKeyboardButton -> listOf(inlineKeyboardButton) }
                    .toCollection(ArrayList())

            val replyKeyboardMarkup = InlineKeyboardMarkup()
            replyKeyboardMarkup.setKeyboard(rowsInline)
            return replyKeyboardMarkup;
        }
    }
}