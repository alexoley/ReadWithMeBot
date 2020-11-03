package org.stream.bot.configs


import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import com.google.auth.Credentials
import com.google.auth.http.HttpCredentialsAdapter
import com.google.auth.oauth2.GoogleCredentials
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile


@Configuration
@Profile("!local")
class GoogleDriveConfig {

    private val APPLICATION_NAME = "ReadWithMeBot"
    private val JSON_FACTORY: JacksonFactory = JacksonFactory.getDefaultInstance()

    /**
     * Global instance of the scopes required by this quickstart.
     * If modifying these scopes, delete your previously saved tokens/ folder.
     */
    private val SCOPES = listOf(DriveScopes.DRIVE)

    @Bean
    fun getNetHttpTransport(): NetHttpTransport {
        return GoogleNetHttpTransport.newTrustedTransport()
    }

    @Bean
    fun getServiceAccountCredentials(
            @Value("\${google.service.account.credentials}") googleServiceAccountCredentials: String): Credentials? {
        return GoogleCredentials.fromStream(googleServiceAccountCredentials.byteInputStream(Charsets.UTF_8))?.
        createScoped(SCOPES)

    }

    @Bean
    fun getDriveService(HTTP_TRANSPORT: NetHttpTransport/*, credential: Credential*/, credentials: Credentials): Drive {
        return Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, HttpCredentialsAdapter(credentials))
                .setApplicationName(APPLICATION_NAME)
                .build()
    }
}