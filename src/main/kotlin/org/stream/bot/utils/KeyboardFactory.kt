package org.stream.bot.utils

import org.stream.bot.entities.FileInfo
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow


open class KeyboardFactory {
    companion object {

        fun removeKeyboard(): ReplyKeyboard {
            return ReplyKeyboardRemove.builder().removeKeyboard(true).build()
        }

        fun cancelButton(keyword: String): ReplyKeyboard =
                ReplyKeyboardMarkup.builder()
                        .keyboard(listOf(KeyboardRow(keyword)))
                        .oneTimeKeyboard(true)
                        .resizeKeyboard(true)
                        .build()

        fun inlineBookDeleteKeyboardFromList(bookList: List<FileInfo>): ReplyKeyboard =
                InlineKeyboardMarkup.builder()
                        .keyboard(bookList.map { fileInfo ->
                            InlineKeyboardRow(InlineKeyboardButton.builder()
                                    .text(fileInfo.fileName)
                                    .callbackData(fileInfo.checksum)
                                    .build())
                        })
                        .build()

        fun inlineBookGiveMoreKeyboard(fileInfo: FileInfo): ReplyKeyboard {
            val text = if (fileInfo.fileName.length > 10)
                "${fileInfo.fileName.subSequence(0, 10)}...\uD83D\uDC49"
            else
                "${fileInfo.fileName}...\uD83D\uDC49"
            val button = InlineKeyboardButton.builder().text(text).callbackData("getnext:${fileInfo.checksum}").build()
            return InlineKeyboardMarkup.builder().keyboard(listOf(InlineKeyboardRow(button))).build()
        }
    }
}