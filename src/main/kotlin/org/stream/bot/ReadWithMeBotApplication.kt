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

//TODO: 1. Move code that sends messages to user from bot to specific classes
// 2. Move all message text to properties(create internalization)
// 3. Add functionality to remove file from persist storage when remove FileInfo from database
// 4. Deal with MarkdownV2(problem with escaping special characters)