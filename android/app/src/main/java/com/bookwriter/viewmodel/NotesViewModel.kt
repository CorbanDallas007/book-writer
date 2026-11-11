package com.bookwriter.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bookwriter.model.Note
import com.bookwriter.model.StoryContext
import com.bookwriter.service.TextProcessingService
import kotlinx.coroutines.launch

class NotesViewModel(
    private val textProcessingService: TextProcessingService
) : ViewModel() {

    private val _notes = mutableListOf<Note>()
    val notes: List<Note> get() = _notes

    var selectedNote: Note? = null
        private set

    fun addNote(note: Note) {
        _notes.add(note)
    }

    fun deleteNote(note: Note) {
        _notes.remove(note)
    }

    fun reprocessNote(note: Note, style: String) {
        val context: List<StoryContext> = emptyList()
        val corrected = textProcessingService.cleanText(note.rawText, context, style)
        note.correctedText = corrected
        note.processingStyle = style
    }

    fun loadDemoNotes() {
        viewModelScope.launch {
            _notes.add(
                Note(
                    title = "Запись демо",
                    rawText = "ну короче ГГ зашел в таверну типа Таинственный Дракон",
                    correctedText = "ГГ зашел в таверну Таинственный Дракон",
                    processingStyle = "literal"
                )
            )
        }
    }
}