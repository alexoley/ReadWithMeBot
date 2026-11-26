package org.stream.bot.services.impl.persistManagers

import org.telegram.telegrambots.meta.generics.TelegramClient
import org.telegram.telegrambots.meta.api.methods.GetFile
import org.telegram.telegrambots.meta.exceptions.TelegramApiException

abstract class AbstractTelegramPersistManager{

    open lateinit var telegramClient: TelegramClient

    //TODO: Think about where to place implementation of this method. Maybe in interface default method
    @Throws(TelegramApiException::class)
    fun downloadTelegramFileWithId(fileId: String): org.telegram.telegrambots.meta.api.objects.File {
        return telegramClient.execute(GetFile.builder().fileId(fileId).build())
    }
}