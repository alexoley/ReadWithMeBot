package org.stream.bot.utils

import org.telegram.abilitybots.api.util.AbilityUtils
import org.telegram.telegrambots.meta.api.objects.Update

fun Update.getChatFirstName(): String =
        if (AbilityUtils.isGroupUpdate(this))
            this.message.chat.title.orEmpty()
        else
            this.message.from.firstName.orEmpty()

fun Update.getChatLastName(): String =
        if (AbilityUtils.isGroupUpdate(this))
            this.message.chat.lastName.orEmpty()
        else
            this.message.from.lastName.orEmpty()

fun Update.getChatUsername(): String =
        if (AbilityUtils.isGroupUpdate(this))
            this.message.chat.firstName.orEmpty()
        else
            this.message.from.userName.orEmpty()

fun Update.getChatType(): ChatType =
        if (AbilityUtils.isGroupUpdate(this))
            ChatType.GROUP
        else
            ChatType.USER

