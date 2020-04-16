package org.stream.bot.services

import org.telegram.telegrambots.meta.api.objects.Update

interface IDocumentPersistManager{
    fun persistToStorage(update: Update, filename: String)
}