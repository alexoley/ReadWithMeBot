package org.stream.bot

import org.stream.bot.entities.Chat
import org.stream.bot.entities.FileInfo
import org.stream.bot.utils.ChatType
import org.stream.bot.utils.Subscribers
import org.telegram.telegrambots.meta.api.objects.CallbackQuery
import org.telegram.telegrambots.meta.api.objects.Document
import org.telegram.telegrambots.meta.api.objects.message.Message
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.api.objects.User
import org.telegram.telegrambots.meta.api.objects.chat.Chat as TelegramChat

/** Builders for Telegram objects and domain entities used across tests. */
object TestFixtures {

    const val CHAT_ID = 100L
    const val CHAT_ID_STR = "100"
    const val GROUP_ID = -500L

    fun user(languageCode: String = "en"): User {
        val user = User(CHAT_ID, "John", false)
        user.lastName = "Doe"
        user.userName = "johndoe"
        user.languageCode = languageCode
        return user
    }

    fun privateChat(): TelegramChat {
        val chat = TelegramChat(CHAT_ID, "private")
        chat.firstName = "John"
        chat.lastName = "Doe"
        chat.userName = "johndoe"
        return chat
    }

    fun groupChat(): TelegramChat {
        val chat = TelegramChat(GROUP_ID, "group")
        chat.title = "Book club"
        return chat
    }

    fun message(text: String? = null, chat: TelegramChat = privateChat(), from: User = user()): Message {
        val message = Message()
        message.messageId = 1
        message.chat = chat
        message.from = from
        message.text = text
        return message
    }

    fun textUpdate(text: String? = "/start", chat: TelegramChat = privateChat(), languageCode: String = "en"): Update {
        val update = Update()
        update.updateId = 1
        update.message = message(text, chat, user(languageCode))
        return update
    }

    fun document(fileName: String = "book.txt",
                 mimeType: String = "text/plain",
                 fileSize: Long = 1024,
                 fileId: String = "telegram-file-id"): Document {
        val document = Document()
        document.fileId = fileId
        document.fileName = fileName
        document.mimeType = mimeType
        document.fileSize = fileSize
        return document
    }

    fun documentUpdate(document: Document = document()): Update {
        val update = textUpdate(text = null)
        update.message.document = document
        return update
    }

    fun callbackUpdate(data: String): Update {
        val callbackQuery = CallbackQuery()
        callbackQuery.id = "cb"
        callbackQuery.from = user()
        callbackQuery.data = data
        callbackQuery.message = message()
        val update = Update()
        update.updateId = 2
        update.callbackQuery = callbackQuery
        return update
    }

    fun fileInfo(fileName: String = "book.txt",
                 checksum: String = "0123456789abcdef0123456789abcdef",
                 mimeType: String = "text/plain",
                 stillReading: Boolean = true) = FileInfo(
            absolutePath = "/tmp/$fileName",
            relativePath = "bookshelf/$fileName",
            fileName = fileName,
            fileNameInSystem = "1_$fileName",
            mimeType = mimeType,
            fileSize = 1024L,
            checksum = checksum,
            stillReading = stillReading)

    fun chat(vararg files: FileInfo, limit: Int = 2) = Chat(
            id = CHAT_ID_STR,
            firstName = "John",
            subscriber = Subscribers.TELEGRAM,
            quantityBookLimit = limit,
            fileList = arrayListOf(*files),
            chatType = ChatType.USER)
}
