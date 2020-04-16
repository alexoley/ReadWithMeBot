package org.stream.bot.configs

import com.mongodb.MongoCredential
import com.mongodb.ServerAddress
import com.mongodb.reactivestreams.client.MongoClient
import com.mongodb.reactivestreams.client.MongoClients
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.mongodb.config.AbstractReactiveMongoConfiguration


//@Configuration
//@EnableReactiveMongoRepositories("org.stream.bot.repositories")
class MongoConfig(@Value("\${spring.data.mongodb.uri}")val connectionString: String) : AbstractReactiveMongoConfiguration() {

    override fun reactiveMongoClient(): MongoClient {
        val mongoCredential: MongoCredential= MongoCredential.createCredential("userName","database","password".toCharArray())
        val serverAddress = ServerAddress("localhost", 27017)
        return MongoClients.create(connectionString)
    }

    override fun getDatabaseName(): String {
        return "bot"
    }

}