package org.stream.bot.services.impl

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.mock
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.stream.bot.TestFixtures
import org.stream.bot.entities.Chat
import org.stream.bot.repositories.UserReactiveRepository
import org.stream.bot.utils.Subscribers
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

class ChatServiceTest {

    private lateinit var repository: UserReactiveRepository
    private lateinit var service: ChatService
    private val chat = TestFixtures.chat()

    @BeforeEach
    fun setUp() {
        repository = mock(UserReactiveRepository::class.java)
        service = ChatService()
        service.userReactiveRepository = repository
    }

    @Test
    fun `saves chat when it does not exist yet`() {
        `when`(repository.existsByIdAndSubscriber(chat.id, Subscribers.TELEGRAM)).thenReturn(Mono.just(false))
        `when`(repository.save(chat)).thenReturn(Mono.just(chat))

        service.saveUserIfNotExist(chat)

        verify(repository).save(chat)
    }

    @Test
    fun `does not overwrite existing chat`() {
        `when`(repository.existsByIdAndSubscriber(chat.id, Subscribers.TELEGRAM)).thenReturn(Mono.just(true))

        service.saveUserIfNotExist(chat)

        verify(repository, never()).save(any(Chat::class.java))
    }

    @Test
    fun `delegates reads and saves to repository`() {
        `when`(repository.findByIdAndSubscriber(chat.id, Subscribers.TELEGRAM)).thenReturn(Mono.just(chat))
        `when`(repository.findAll()).thenReturn(Flux.just(chat))
        `when`(repository.save(chat)).thenReturn(Mono.just(chat))

        assertEquals(chat, service.getUserByIdAndSubscriber(chat.id, Subscribers.TELEGRAM).block())
        assertEquals(listOf(chat), service.getAllUsers().collectList().block())
        assertEquals(chat, service.saveUser(chat).block())
    }
}
