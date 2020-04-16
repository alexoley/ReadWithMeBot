package org.stream.bot.entities

data class FileInfo(val absolutePath: String?,
                    val relativePath: String?,
                    val fileName: String,
                    val mimeType: String,
                    val fileSize: Int,
                    val hash: String,
                    //name of book without prefix
                    val nativeFileName: String,
                    val cronExpression: String?,
                    val lastSentPage: Int)