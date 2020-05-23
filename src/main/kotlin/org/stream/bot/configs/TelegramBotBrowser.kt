package org.stream.bot.configs

import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp.Browser
import com.google.api.client.util.Preconditions
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.stream.bot.Bot
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import java.awt.Desktop
import java.io.IOException
import java.net.URI

@Component
class TelegramBotBrowser : AuthorizationCodeInstalledApp.Browser {

    private val logger = LoggerFactory.getLogger(javaClass)

    @Autowired
    lateinit var bot: Bot

    override fun browse(url: String?) {
        Preconditions.checkNotNull(url)
        // Ask user to open in their browser using copy-paste
        bot.execute(SendMessage()
                .setChatId(bot.creatorId().toLong())
                .setText("Please open the following address in your browser:\n$url"))
        // Attempt to open it in the browser
        try {
            if (Desktop.isDesktopSupported()) {
                val desktop = Desktop.getDesktop()
                if (desktop.isSupported(Desktop.Action.BROWSE)) {
                    logger.info("Attempting to open that address in the default browser now...")
                    desktop.browse(URI.create(url))
                }
            }
        } catch (e: IOException) {
            logger.warn("Unable to open browser", e)
        } catch (e: InternalError) {
            // A bug in a JRE can cause Desktop.isDesktopSupported() to throw an
            // InternalError rather than returning false. The error reads,
            // "Can't connect to X11 window server using ':0.0' as the value of the
            // DISPLAY variable." The exact error message may vary slightly.
            logger.warn("Unable to open browser", e)
        }
    }
}