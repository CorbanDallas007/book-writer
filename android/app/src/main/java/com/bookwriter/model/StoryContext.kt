package com.bookwriter.model

data class StoryContext(
    val id: String = java.util.UUID.randomUUID().toString(),
    var keyword: String,
    var details: String = ""
)