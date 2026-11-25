package org.stream.bot.services.impl.persistManagers

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.doReturn
import org.mockito.Mockito.mock
import org.stream.bot.Bot
import org.stream.bot.TestFixtures
import org.stream.bot.exceptions.DublicateBookException
import org.telegram.telegrambots.meta.api.methods.GetFile
import java.io.File
import java.security.MessageDigest
import org.telegram.telegrambots.meta.api.objects.File as TelegramFile

class LocalDocumentTelegramPersistManagerTest {

    @TempDir
    lateinit var dir: File

    private lateinit var bot: Bot
    private lateinit var manager: LocalDocumentTelegramPersistManagerWithInputStream
    private val content = "Some book content".toByteArray()
    private val md5 = MessageDigest.getInstance("MD5").digest(content).joinToString("") { "%02x".format(it) }

    @BeforeEach
    fun setUp() {
        bot = mock(Bot::class.java)
        val telegramFile = TelegramFile()
        telegramFile.fileId = "telegram-file-id"
        telegramFile.filePath = "documents/file.txt"
        doReturn(telegramFile).`when`(bot).execute(any(GetFile::class.java))
        doReturn(content.inputStream()).`when`(bot).downloadFileAsStream(any(TelegramFile::class.java))

        manager = LocalDocumentTelegramPersistManagerWithInputStream()
        manager.bot = bot
        manager.bookDirectory = File(dir, "bookshelf").path
        manager.init()
    }

    @Test
    fun `init creates books directory`() {
        assertTrue(File(manager.bookDirectory).isDirectory)
    }

    @Test
    fun `persists telegram file and computes md5 checksum`() {
        val update = TestFixtures.documentUpdate(TestFixtures.document(fileName = "book.txt", fileSize = 17))

        val info = manager.persistToStorage(update, "123_book.txt", arrayListOf())

        val stored = File(info.relativePath)
        assertTrue(stored.exists())
        assertEquals(content.toList(), stored.readBytes().toList())
        assertEquals(md5, info.checksum)
        assertEquals("book.txt", info.fileName)
        assertEquals("123_book.txt", info.fileNameInSystem)
        assertEquals("text/plain", info.mimeType)
        assertEquals(17, info.fileSize)
        assertEquals(stored.absolutePath, info.absolutePath)
    }

    @Test
    fun `duplicate book is rejected and its file removed`() {
        val update = TestFixtures.documentUpdate()
        val existing = TestFixtures.fileInfo(checksum = md5.toUpperCase())

        assertThrows(DublicateBookException::class.java) {
            manager.persistToStorage(update, "123_book.txt", arrayListOf(existing))
        }
        assertFalse(File(manager.bookDirectory, "123_book.txt").exists())
    }

    @Test
    fun `download returns stored file and remove deletes it`() {
        val info = manager.persistToStorage(TestFixtures.documentUpdate(), "123_book.txt", arrayListOf())

        assertEquals(content.toList(), manager.downloadFromStorage(info).readBytes().toList())
        assertTrue(manager.removeFromStorage(info))
        assertFalse(File(info.relativePath).exists())
        assertFalse(manager.removeFromStorage(info))
    }
}
