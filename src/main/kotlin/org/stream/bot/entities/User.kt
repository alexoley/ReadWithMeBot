package org.stream.bot.entities

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import org.stream.bot.utils.Subscribers

@Document(collection = "users")
data class User(@Id val id: String,
                val firstName: String? = "",
                val lastName: String? = "",
                val nickname: String? = "",
                val subscriber: Subscribers,
                var quantityBookLimit: Int = 5,
                val fileList: ArrayList<FileInfo>? = arrayListOf())