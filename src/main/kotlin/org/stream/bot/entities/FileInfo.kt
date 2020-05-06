package org.stream.bot.entities

data class FileInfo(val absolutePath: String? = "",
                    val relativePath: String,
                    val fileName: String,
                    val fileNameInSystem: String,
                    val mimeType: String,
                    val fileSize: Int,
                    val checksum: String,
                    val cronExpression: String? = "",
                    var lastSentPage: DocumentPosition = DocumentPosition(),
                    var stillReading: Boolean = true)