package com.bookwriter.viewmodel

import androidx.lifecycle.ViewModel
import com.bookwriter.model.Chapter
import com.bookwriter.model.Note
import com.bookwriter.model.StoryContext

class BookTreeViewModel : ViewModel() {

    private val _chapters = mutableListOf<Chapter>()
    val chapters: List<Chapter> get() = _chapters

    private val _notes = mutableListOf<Note>()
    val notes: List<Note> get() = _notes

    private val _storyContexts = mutableListOf<StoryContext>()
    val storyContexts: List<StoryContext> get() = _storyContexts

    fun addChapter(name: String, order: Int? = null) {
        val nextOrder = order ?: (_chapters.maxOfOrNull { it.order } ?: 0) + 1
        _chapters.add(Chapter(name = name, order = nextOrder))
    }

    fun addStoryContext(keyword: String, details: String = "") {
        _storyContexts.add(StoryContext(keyword = keyword, details = details))
    }

    fun notesForChapter(chapter: Chapter): List<Note> {
        return _notes.filter { it.chapterId == chapter.id }
    }

    fun loadDemoData() {
        _chapters.add(Chapter(name = "Глава 1. Пролог", order = 1))
        _chapters.add(Chapter(name = "Глава 2. Встреча", order = 2))
        _chapters.add(Chapter(name = "Глава 3. Побег", order = 3))
        _storyContexts.add(StoryContext(keyword = "Элронд", details = "Персонаж, эльф"))
        _storyContexts.add(StoryContext(keyword = "Таинственный Дракон", details = "Локация, таверна"))
    }
}