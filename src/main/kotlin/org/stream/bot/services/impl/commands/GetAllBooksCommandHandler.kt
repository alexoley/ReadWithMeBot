package org.stream.bot.services.impl.commands

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.stream.bot.Bot
import org.stream.bot.services.ICommandHandler
import org.stream.bot.services.IUserService
import org.stream.bot.services.MARKDOWN_ENABLED
import org.stream.bot.utils.Subscribers
import org.telegram.abilitybots.api.util.AbilityUtils
import org.telegram.abilitybots.api.util.AbilityUtils.getLocalizedMessage
import org.telegram.abilitybots.api.util.AbilityUtils.getUser
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.Update

@Service
class GetAllBooksCommandHandler : ICommandHandler {

    private val logger = LoggerFactory.getLogger(javaClass)

    @Autowired
    lateinit var bot: Bot

    @Autowired
    lateinit var userService: IUserService

    override fun answer(update: Update) {
        val user = userService.getUserByIdAndSubscriber(AbilityUtils.getChatId(update).toString(),
                Subscribers.TELEGRAM).block()
        val sendText: String? = if (user?.fileList.isNullOrEmpty())
            getLocalizedMessage("books.list.command.have.no.books",
                    getUser(update).languageCode)
        else
            getLocalizedMessage("books.list.command.book.list",
                    getUser(update).languageCode) +
                    user?.fileList?.asSequence()
                            ?.mapIndexed { index, fileInfo -> "${index + 1}. ${fileInfo.fileName}\n" }
                            ?.reduce { acc, s -> acc + s }

        bot.execute(SendMessage()
                .setText(sendText.botText())
                .setChatId(AbilityUtils.getChatId(update))
                .enableMarkdown(MARKDOWN_ENABLED))
    }

    override fun firstReply(update: Update) {
        TODO("Not yet implemented")
    }

    override fun secondReply(update: Update) {
        TODO("Not yet implemented")
    }

    override fun thirdReply(update: Update) {
        TODO("Not yet implemented")
    }
}