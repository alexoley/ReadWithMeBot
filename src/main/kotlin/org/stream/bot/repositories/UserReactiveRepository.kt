package org.stream.bot.repositories

import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository
import org.stream.bot.entities.Chat
import org.stream.bot.utils.Subscribers
import reactor.core.publisher.Mono

@Repository
interface UserReactiveRepository: ReactiveCrudRepository<Chat, String>{

    fun findByIdAndSubscriber(id: String, subscriber: Subscribers): Mono<Chat>

    fun existsByIdAndSubscriber(id: String, subscriber: Subscribers): Mono<Boolean>
}