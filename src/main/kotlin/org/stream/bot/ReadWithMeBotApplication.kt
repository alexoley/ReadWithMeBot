package org.stream.bot

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.data.mongodb.repository.config.EnableReactiveMongoRepositories
import org.springframework.scheduling.annotation.EnableAsync
import org.springframework.scheduling.annotation.EnableScheduling
import org.telegram.telegrambots.ApiContextInitializer

@SpringBootApplication
@EnableScheduling
class ReadWithMeBotApplication

	fun main(args: Array<String>) {
		// Initializes dependencies necessary for the base bot
		ApiContextInitializer.init()
		runApplication<ReadWithMeBotApplication>(*args)
	}

//TODO: Rename commands: addbook->add, mybooks->books, removebook->remove