package org.stream.bot.configs

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.newFixedThreadPoolContext
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import kotlin.coroutines.CoroutineContext

@Configuration
class AppConfig {

    @Bean
    fun correlateCoroutineContext(@Value("\${system.pool-size}") poolSize: Int): CoroutineContext =
            newFixedThreadPoolContext(
                    poolSize, "correlate-thread"
            )

    @Bean
    fun correlateCoroutineScope(correlateCoroutineContext: CoroutineContext) = object : CoroutineScope {
        override val coroutineContext = correlateCoroutineContext
    }
}