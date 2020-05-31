package org.stream.bot.services.impl

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.stream.bot.services.IChatService
import org.stream.bot.services.impl.commands.BookPartSenderService
import java.util.function.Consumer

@Service
class Scheduler {

    private val logger = LoggerFactory.getLogger(javaClass)

    @Autowired
    lateinit var chatService: IChatService

    @Autowired
    lateinit var bookPartSenderService: BookPartSenderService

    companion object {
        private const val cronexpression = "0 0 19 * * *"
    }

    @Scheduled(cron = cronexpression)
    fun scheduler() {
        logger.info("Scheduler sender run.")
        chatService.getAllUsers()
                .filter { chat ->
                    if (chat != null) chat.fileList.isNullOrEmpty().not() else false
                }
                .subscribe(
                        Consumer {
                            it.fileList.filter { fileInfo -> fileInfo.stillReading }
                                    .forEach(Consumer { fileInfo -> bookPartSenderService.sendMessageAndUpdateFileInfo(fileInfo, it.id) })
                            chatService.saveUser(it).subscribe()
                        },
                        Consumer {
                            logger.error(it.message)
                            it.printStackTrace()
                        })
    }
}