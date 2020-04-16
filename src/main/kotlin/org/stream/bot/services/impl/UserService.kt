package org.stream.bot.services.impl

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.stream.bot.entities.FileInfo
import org.stream.bot.entities.User
import org.stream.bot.repositories.UserReactiveRepository
import org.stream.bot.services.IUserService
import org.telegram.telegrambots.meta.api.objects.Update

@Service
class UserService : IUserService {

    @Autowired
    lateinit var userReactiveRepository: UserReactiveRepository

    override fun saveUserIfNotExist(user: User) {
        if(!userReactiveRepository.existsByIdAndSubscriber(user.id, user.subscriber).block()!!){
            userReactiveRepository.save(user).block()
        }
    }

    override fun addBookIfNotExists(user: User, update: Update) {
        val monoUser = userReactiveRepository.findByIdAndSubscriber(user.id,user.subscriber)
        /*val fileInfo= FileInfo(fileName = update.message.document.fileName,
                mimeType = update.message.document.mimeType,
                fileSize = update.message.document.fileSize)*/
        TODO("not implemented")
    }

    override fun removeBookFromUser(user: User, fileInfo: FileInfo) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getAllUserBooks(user: User): ArrayList<FileInfo> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getAllUserBooksHash(books: ArrayList<FileInfo>): List<String> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}