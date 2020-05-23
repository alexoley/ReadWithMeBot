package org.stream.bot.services.impl

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.stream.bot.entities.Chat
import org.stream.bot.repositories.UserReactiveRepository
import org.stream.bot.services.IUserService
import org.stream.bot.utils.Subscribers
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import org.slf4j.LoggerFactory
import java.util.function.Consumer

@Service
class UserService : IUserService {

    private val logger = LoggerFactory.getLogger(javaClass)

    @Autowired
    lateinit var userReactiveRepository: UserReactiveRepository

    override fun getUserByIdAndSubscriber(id: String, subscriber: Subscribers): Mono<Chat> {
        return userReactiveRepository.findByIdAndSubscriber(id, subscriber)
    }

    override fun saveUserIfNotExist(chat: Chat){
        userReactiveRepository.existsByIdAndSubscriber(chat.id, chat.subscriber)
                .subscribe(
                        Consumer { t ->
                            if (t!=null && t.not()){
                                userReactiveRepository.save(chat).subscribe()
                            }
                        },
                        Consumer { t -> logger.error(t.message) }
                )
    }

    override fun saveUser(chat: Chat): Mono<Chat>{
        return userReactiveRepository.save(chat)
    }

    override fun getAllUsers(): Flux<Chat> {
        return userReactiveRepository.findAll()
    }
}