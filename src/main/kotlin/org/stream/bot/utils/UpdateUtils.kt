package org.stream.bot.utils

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.telegram.abilitybots.api.sender.MessageSender
import org.telegram.abilitybots.api.util.AbilityUtils
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.Update
import java.util.Objects.nonNull
import java.util.function.Predicate

class UpdateUtils{

    fun UpdateUtils.ifUpdateHasAcceptableSize(update: Update): Boolean {
        return nonNull(update) && update.message.document.fileSize>BotConstants.MAX_TELEGRAM_FILE_SIZE
    }
}