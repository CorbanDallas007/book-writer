package com.bookwriter.dao

import com.bookwriter.database.BookDatabase
import com.bookwriter.database.NoteEntity
import com.bookwriter.model.Note
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
class NoteDaoTest {

    private lateinit var database: BookDatabase
    private lateinit var noteDao: NoteDao

    @Mock
    lateinit var context: android.content.Context

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        database = BookDatabase.getDatabase(context = context)
        noteDao = database.noteDao()
        // Clear database before each test
        runBlocking {
            noteDao.deleteAll()
        }
    }

    @After
    fun tearDown() {
        runBlocking {
            noteDao.deleteAll()
        }
    }

    @Test
    fun `insert and retrieve note`() {
        // Arrange
        val note = Note(
            id = java.util.UUID.randomUUID().toString(),
            title = "Test Note",
            rawText = "Sample text",
            correctedText = "Sample text",
            processingStyle = "literal"
        )

        // Act
        runBlocking {
            noteDao.insertNote(
                NoteEntity(
                    id = note.id,
                    title = note.title,
                    rawText = note.rawText,
                    correctedText = note.correctedText,
                    createdAt = System.currentTimeMillis(),
                    chapterId = null,
                    processingStyle = note.processingStyle
                )
            )
            val retrieved = noteDao.getNoteById(note.id)
            
            // Assert
            assert(retrieved != null)
            assert(retrieved.title == note.title)
            assert(retrieved.rawText == note.rawText)
        }
    }

    @Test
    fun `get_all_notes_returns_empty_list_when_no_notes()`() {
        // Act
        val notes = runBlocking {
            noteDao.getAllNotes()
        }
        
        // Assert
        assert(notes.isEmpty())
    }

    @Test
    fun `get_notes_by_chapter_returns_notes`()() {
        // Arrange
        val chapterId = java.util.UUID.randomUUID().toString()
        val note1 = Note(
            id = java.util.UUID.randomUUID().toString(),
            title = "Note 1",
            rawText = "Text 1",
            processingStyle = "literal"
        )
        val note2 = Note(
            id = java.util.UUID.randomUUID().toString(),
            title = "Note 2",
            rawText = "Text 2",
            processingStyle = "literal"
        )

        // Act
        runBlocking {
            noteDao.insertNote(
                NoteEntity(
                    id = note1.id,
                    title = note1.title,
                    rawText = note1.rawText,
                    correctedText = note1.rawText,
                    createdAt = System.currentTimeMillis(),
                    chapterId = chapterId,
                    processingStyle = note1.processingStyle
                )
            )
            noteDao.insertNote(
                NoteEntity(
                    id = note2.id,
                    title = note2.title,
                    rawText = note2.rawText,
                    correctedText = note2.rawText,
                    createdAt = System.currentTimeMillis(),
                    chapterId = chapterId,
                    processingStyle = note2.processingStyle
                )
            )
            val notes = noteDao.getNotesByChapter(chapterId)
            
            // Assert
            assert(notes.size == 2)
            assert(notes.any { it.title == "Note 1" })
            assert(notes.any { it.title == "Note 2" })
        }
    }
}