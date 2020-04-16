package org.stream.bot

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.data.mongodb.repository.config.EnableReactiveMongoRepositories
import org.telegram.telegrambots.ApiContextInitializer

@SpringBootApplication
class ReadWithMeBotApplication

	fun main(args: Array<String>) {
		// Initializes dependencies necessary for the base bot
		ApiContextInitializer.init()
		runApplication<ReadWithMeBotApplication>(*args)
	}
