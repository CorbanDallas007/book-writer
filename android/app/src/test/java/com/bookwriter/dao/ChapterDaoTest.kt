package com.bookwriter.dao

import com.bookwriter.database.BookDatabase
import com.bookwriter.database.ChapterEntity
import com.bookwriter.model.Chapter
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.runners.MockitoJUnitRunner
import java.util.*

@RunWith(MockitoJUnitRunner::class)
class ChapterDaoTest {

    private lateinit var database: BookDatabase
    private lateinit var chapterDao: ChapterDao

    @Mock
    lateinit var context: android.content.Context

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        database = BookDatabase.getDatabase(context = context)
        chapterDao = database.chapterDao()
        // Clear database before each test
        runBlocking {
            chapterDao.deleteAll()
        }
    }

    @After
    fun tearDown() {
        runBlocking {
            chapterDao.deleteAll()
        }
    }

    @Test
    fun `insert and retrieve chapter`() {
        // Arrange
        val chapter = Chapter(
            id = java.util.UUID.randomUUID().toString(),
            name = "Test Chapter",
            order = 1
        )

        // Act
        runBlocking {
            chapterDao.insertChapter(
                ChapterEntity(
                    id = chapter.id,
                    name = chapter.name,
                    order = chapter.order
                )
            )
            val retrieved = chapterDao.getChapterById(chapter.id)
            
            // Assert
            assert(retrieved != null)
            assert(retrieved.name == chapter.name)
            assert(retrieved.order == chapter.order)
        }
    }

    @Test
    fun `get_all_chapters_returns_empty_list_when_no_chapters()`() {
        // Act
        val chapters = runBlocking {
            chapterDao.getAllChapters()
        }
        
        // Assert
        assert(chapters.isEmpty())
    }

    @Test
    fun `get_all_chapters_returns_inserted_chapters()`() {
        // Arrange
        val chapter1 = Chapter(
            id = java.util.UUID.randomUUID().toString(),
            name = "Chapter 1",
            order = 1
        )
        val chapter2 = Chapter(
            id = java.util.UUID.randomUUID().toString(),
            name = "Chapter 2",
            order = 2
        )

        // Act
        runBlocking {
            chapterDao.insertChapter(
                ChapterEntity(
                    id = chapter1.id,
                    name = chapter1.name,
                    order = chapter1.order
                )
            )
            chapterDao.insertChapter(
                ChapterEntity(
                    id = chapter2.id,
                    name = chapter2.name,
                    order = chapter2.order
                )
            )
            val chapters = chapterDao.getAllChapters()
            
            // Assert
            assert(chapters.size == 2)
            assert(chapters.any { it.name == "Chapter 1" })
            assert(chapters.any { it.name == "Chapter 2" })
        }
    }
}