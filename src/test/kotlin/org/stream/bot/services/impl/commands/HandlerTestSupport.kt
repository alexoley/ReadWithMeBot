package org.stream.bot.services.impl.commands

import org.mockito.ArgumentCaptor
import org.mockito.Mockito.atLeastOnce
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.stream.bot.Bot
import org.telegram.abilitybots.api.util.AbilityUtils
import org.telegram.telegrambots.meta.api.methods.send.SendMessage

fun mockBot(): Bot = mock(Bot::class.java)

/** Localized text exactly as handlers build it for an "en" user. */
fun msg(key: String, vararg args: Any): String = AbilityUtils.getLocalizedMessage(key, "en", *args)

/** All SendMessage objects passed to bot.execute, in order. */
fun sentMessages(bot: Bot): List<SendMessage> {
    val captor = ArgumentCaptor.forClass(SendMessage::class.java)
    verify(bot, atLeastOnce()).execute(captor.capture())
    return captor.allValues
}

fun lastSentMessage(bot: Bot): SendMessage = sentMessages(bot).last()
