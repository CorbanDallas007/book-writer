package com.bookwriter.model

data class Note(
    val id: String = java.util.UUID.randomUUID().toString(),
    var title: String = "Новая запись",
    var rawText: String = "",
    var correctedText: String = "",
    var createdAt: Long = java.lang.System.currentTimeMillis(),
    var chapterId: String? = null,
    var processingStyle: String = "literal"
)