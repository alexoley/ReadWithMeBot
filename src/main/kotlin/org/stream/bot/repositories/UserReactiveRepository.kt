package org.stream.bot.repositories

import org.springframework.data.mongodb.repository.Query
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository
import org.stream.bot.entities.User
import org.stream.bot.utils.Subscribers
import reactor.core.publisher.Mono

@Repository
interface UserReactiveRepository: ReactiveCrudRepository<User, String>{

    fun findByIdAndSubscriber(id: String, subscriber: Subscribers): Mono<User>

    fun existsByIdAndSubscriber(id: String, subscriber: Subscribers): Mono<Boolean>
}