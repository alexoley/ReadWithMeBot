package org.stream.bot

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication
@EnableScheduling
class ReadWithMeBotApplication

	fun main(args: Array<String>) {
		runApplication<ReadWithMeBotApplication>(*args)
	}

//TODO: Rename commands: addbook->add, mybooks->books, removebook->remove