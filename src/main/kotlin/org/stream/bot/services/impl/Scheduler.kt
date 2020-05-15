package org.stream.bot.services.impl

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.stream.bot.services.IUserService
import java.util.function.Consumer

@Service
class Scheduler {

    private val logger = LoggerFactory.getLogger(javaClass)

    @Autowired
    lateinit var userService: IUserService

    @Autowired
    lateinit var bookPartSender: BookPartSender

    companion object {
        private const val cronexpression = "0 0 19 * * *"
    }

    @Scheduled(cron = cronexpression)
    fun scheduler() {
        logger.info("Scheduler sender run.")
        userService.getAllUsers()
                .filter { user ->
                    if (user != null) user.fileList.isNullOrEmpty().not() else false
                }
                .subscribe(
                        Consumer {
                            it.fileList.filter { fileInfo -> fileInfo.stillReading }
                                    .forEach(Consumer { fileInfo -> bookPartSender.sendMessageAndUpdate(fileInfo, it.id) })
                            userService.saveUser(it).subscribe()
                        },
                        Consumer { t ->
                            {
                                logger.error(t.message)
                                t.printStackTrace()
                            }
                        })
    }
}