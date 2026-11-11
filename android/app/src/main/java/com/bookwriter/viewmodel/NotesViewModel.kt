package com.bookwriter.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bookwriter.database.BookDatabase
import com.bookwriter.database.NoteEntity
import com.bookwriter.model.Note
import com.bookwriter.service.TextProcessingService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class NotesViewModel(
    private val database: BookDatabase = BookDatabase.getDatabase(
        context = android.app.ApplicationContext
    ),
    private val textProcessingService: TextProcessingService
) : ViewModel() {

    init {
        _notes.value = database.noteDao().getAllNotes()
    }

    private val _notes = MutableLiveData<List<Note>>()
    val notes: List<Note>
        get() = _notes.value ?: emptyList()

    var selectedNote: Note? = null
        private set

    fun addNote(note: Note) {
        viewModelScope.launch(Dispatchers.IO) {
            val entity = NoteEntity(
                id = note.id,
                title = note.title,
                rawText = note.rawText,
                correctedText = note.correctedText,
                createdAt = note.createdAt ?: System.currentTimeMillis(),
                chapterId = note.chapterId,
                processingStyle = note.processingStyle
            )
            database.noteDao().insertNote(entity)
            _notes.postValue(database.noteDao().getAllNotes())
        }
    }

    fun deleteNote(note: Note) {
        viewModelScope.launch(Dispatchers.IO) {
            database.noteDao().deleteNote(NoteEntity(
                id = note.id,
                title = note.title,
                rawText = note.rawText,
                correctedText = note.correctedText,
                createdAt = note.createdAt,
                chapterId = note.chapterId,
                processingStyle = note.processingStyle
            ))
            _notes.postValue(database.noteDao().getAllNotes())
        }
    }

    fun reprocessNote(note: Note, style: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val context: List<com.bookwriter.model.StoryContext> = emptyList()
            val corrected = textProcessingService.cleanText(note.rawText, context, style)
            note.correctedText = corrected
            note.processingStyle = style

            val entity = NoteEntity(
                id = note.id,
                title = note.title,
                rawText = note.rawText,
                correctedText = corrected,
                createdAt = note.createdAt,
                chapterId = note.chapterId,
                processingStyle = style
            )
            database.noteDao().insertNote(entity)
            _notes.postValue(database.noteDao().getAllNotes())
        }
    }

    fun loadDemoNotes() {
        viewModelScope.launch(Dispatchers.IO) {
            _notes.postValue(
                listOf(
                    Note(
                        id = java.util.UUID.randomUUID().toString(),
                        title = "Запись демо",
                        rawText = "ну короче ГГ зашел в таверну типа Таинственный Дракон",
                        correctedText = "ГГ зашел в таверну Таинственный Дракон",
                        processingStyle = "literal"
                    )
                )
            )
            _notes.postValue(database.noteDao().getAllNotes())
        }
    }
}