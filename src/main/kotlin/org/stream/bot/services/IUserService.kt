package org.stream.bot.services

import org.stream.bot.entities.FileInfo
import org.stream.bot.entities.User
import org.telegram.telegrambots.meta.api.objects.Update

interface IUserService {

    fun saveUserIfNotExist(user: User)

    fun addBookIfNotExists(user: User, update: Update)

    fun removeBookFromUser(user: User, fileInfo: FileInfo)

    fun getAllUserBooks(user: User): ArrayList<FileInfo>

    fun getAllUserBooksHash(books: ArrayList<FileInfo>): List<String>
}