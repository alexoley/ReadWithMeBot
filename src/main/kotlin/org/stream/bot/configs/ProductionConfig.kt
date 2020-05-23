package org.stream.bot.configs

import com.google.api.client.auth.oauth2.Credential
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.client.util.store.FileDataStoreFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.stream.bot.Bot
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import java.io.ByteArrayInputStream
import java.io.File
import java.io.InputStreamReader


@Configuration
@Profile("production")
class ProductionConfig {

    private val APPLICATION_NAME = "ReadWithMeBot"
    private val TOKENS_DIRECTORY_PATH = "tokens"
    private val JSON_FACTORY: JacksonFactory = JacksonFactory.getDefaultInstance()

    /**
     * Global instance of the scopes required by this quickstart.
     * If modifying these scopes, delete your previously saved tokens/ folder.
     */
    private val SCOPES = listOf(DriveScopes.DRIVE)

    @Autowired
    lateinit var telegramBotBrowser: TelegramBotBrowser

    @Bean
    fun getNetHttpTransport(): NetHttpTransport {
        return GoogleNetHttpTransport.newTrustedTransport()
    }

    @Bean
    fun getGoogleDriveCredential(@Value("\${google.drive.credentials}") googleDriveCredentials: String,
                                 HTTP_TRANSPORT: NetHttpTransport): Credential{
        val clientSecrets = GoogleClientSecrets.load(JSON_FACTORY,
                InputStreamReader(ByteArrayInputStream(googleDriveCredentials.toByteArray(Charsets.UTF_8))))
        val flow = GoogleAuthorizationCodeFlow.Builder(
                HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
                .setDataStoreFactory(FileDataStoreFactory(File(TOKENS_DIRECTORY_PATH)))
                .setAccessType("offline")
                .build()
        val receiver = LocalServerReceiver.Builder().setPort(8888).build()
        return AuthorizationCodeInstalledApp(flow, receiver, telegramBotBrowser).authorize("user")
    }

    @Bean
    fun getDriveService(HTTP_TRANSPORT: NetHttpTransport, credential: Credential): Drive {
        return Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential)
                .setApplicationName(APPLICATION_NAME)
                .build()
    }
}