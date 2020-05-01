package org.stream.bot.services

import org.stream.bot.entities.FileInfo
import org.stream.bot.entities.User
import org.stream.bot.utils.Subscribers
import org.telegram.telegrambots.meta.api.objects.Update
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

interface IUserService {

    fun getUserByIdAndSubscriber(id: String, subscriber: Subscribers): Mono<User>

    fun saveUserIfNotExist(user: User)

    fun saveUser(user: User): Mono<User>

    fun getAllUsers(): Flux<User>
}