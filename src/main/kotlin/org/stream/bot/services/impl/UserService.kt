package org.stream.bot.services.impl

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.stream.bot.entities.FileInfo
import org.stream.bot.entities.User
import org.stream.bot.repositories.UserReactiveRepository
import org.stream.bot.services.IUserService
import org.stream.bot.utils.Subscribers
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import kotlinx.coroutines.*
import org.slf4j.LoggerFactory
import java.util.function.Consumer

@Service
class UserService : IUserService {

    private val logger = LoggerFactory.getLogger(javaClass)

    @Autowired
    lateinit var userReactiveRepository: UserReactiveRepository

    override fun getUserByIdAndSubscriber(id: String, subscriber: Subscribers): Mono<User> {
        return userReactiveRepository.findByIdAndSubscriber(id, subscriber)
    }

    override fun saveUserIfNotExist(user: User){
        userReactiveRepository.existsByIdAndSubscriber(user.id, user.subscriber)
                .subscribe(
                        Consumer { t ->
                            if (t!=null && t.not()){
                                userReactiveRepository.save(user).subscribe()
                            }
                        },
                        Consumer { t -> logger.error(t.message) }
                )
    }

    override fun saveUser(user: User): Mono<User>{
        return userReactiveRepository.save(user)
    }

    override fun getAllUsers(): Flux<User> {
        return userReactiveRepository.findAll()
    }
}