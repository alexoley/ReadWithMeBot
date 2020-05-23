package org.stream.bot.services

import org.stream.bot.entities.Chat
import org.stream.bot.utils.Subscribers
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

interface IUserService {

    fun getUserByIdAndSubscriber(id: String, subscriber: Subscribers): Mono<Chat>

    fun saveUserIfNotExist(chat: Chat)

    fun saveUser(chat: Chat): Mono<Chat>

    fun getAllUsers(): Flux<Chat>
}