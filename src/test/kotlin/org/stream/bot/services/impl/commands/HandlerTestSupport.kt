package org.stream.bot.services.impl.commands

import org.mockito.ArgumentCaptor
import org.mockito.Mockito.atLeastOnce
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.stream.bot.services.ChatStateStore
import org.telegram.telegrambots.meta.generics.TelegramClient
import org.telegram.telegrambots.abilitybots.api.util.AbilityUtils
import org.telegram.telegrambots.meta.api.methods.send.SendMessage

fun mockClient(): TelegramClient = mock(TelegramClient::class.java)

fun mockStates(): ChatStateStore = mock(ChatStateStore::class.java)

/** Localized text exactly as handlers build it for an "en" user. */
fun msg(key: String, vararg args: Any): String = AbilityUtils.getLocalizedMessage(key, "en", *args)

/** All SendMessage objects passed to bot.execute, in order. */
fun sentMessages(client: TelegramClient): List<SendMessage> {
    val captor = ArgumentCaptor.forClass(SendMessage::class.java)
    verify(client, atLeastOnce()).execute(captor.capture())
    return captor.allValues
}

fun lastSentMessage(client: TelegramClient): SendMessage = sentMessages(client).last()
