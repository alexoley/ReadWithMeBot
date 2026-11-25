package org.stream.bot.services.impl

import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mockito.mock
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.stream.bot.TestFixtures
import org.stream.bot.entities.FileInfo
import org.stream.bot.services.IChatService
import org.stream.bot.services.impl.commands.BookPartSenderService
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

class SchedulerTest {

    @Test
    fun `sends next part of every unfinished book and saves chats`() {
        val reading = TestFixtures.fileInfo(fileName = "reading.txt", checksum = "a")
        val finished = TestFixtures.fileInfo(fileName = "finished.txt", checksum = "b", stillReading = false)
        val activeChat = TestFixtures.chat(reading, finished)
        val emptyChat = TestFixtures.chat().copy(id = "200")

        val chatService = mock(IChatService::class.java)
        val sender = mock(BookPartSenderService::class.java)
        `when`(chatService.getAllUsers()).thenReturn(Flux.just(activeChat, emptyChat))
        `when`(chatService.saveUser(activeChat)).thenReturn(Mono.just(activeChat))

        val scheduler = Scheduler()
        scheduler.chatService = chatService
        scheduler.bookPartSenderService = sender
        scheduler.scheduler()

        verify(sender).sendMessageAndUpdateFileInfo(reading, TestFixtures.CHAT_ID_STR)
        verify(sender, never()).sendMessageAndUpdateFileInfo(finished, TestFixtures.CHAT_ID_STR)
        verify(chatService).saveUser(activeChat)
        verify(chatService, never()).saveUser(emptyChat)
    }
}
