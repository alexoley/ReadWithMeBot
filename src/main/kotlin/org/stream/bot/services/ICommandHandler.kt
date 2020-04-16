package org.stream.bot.services

import org.telegram.telegrambots.meta.api.objects.Update

interface ICommandHandler {

    fun process(update: Update)
}