package org.stream.bot.entities

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import org.stream.bot.utils.ChatType
import org.stream.bot.utils.Subscribers

@Document(collection = "chats")
data class Chat(@Id val id: String,
                val firstName: String? = "",
                val lastName: String? = "",
                val username: String? = "",
                val subscriber: Subscribers,
                var quantityBookLimit: Int = 2,
                val fileList: ArrayList<FileInfo> = arrayListOf(),
                val chatType: ChatType = ChatType.USER)
//TODO: Add location, timezone and ?language?