package org.stream.bot.configs

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.telegram.telegrambots.abilitybots.api.db.DBContext
import org.telegram.telegrambots.abilitybots.api.db.MapDBContext
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient
import org.telegram.telegrambots.meta.generics.TelegramClient

@Configuration
class TelegramConfig {

    /** Client used to call the Telegram Bot API (send messages, download files). */
    @Bean
    fun telegramClient(@Value("\${bot.token}") token: String): TelegramClient = OkHttpTelegramClient(token)

    /** Persistent MapDB storage of the ability bot (users, admins and chat states). */
    @Bean(destroyMethod = "close")
    fun botDbContext(@Value("\${bot.username}") username: String): DBContext = MapDBContext.onlineInstance(username)
}
