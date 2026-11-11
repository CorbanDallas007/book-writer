package com.bookwriter.model

data class Chapter(
    val id: String = java.util.UUID.randomUUID().toString(),
    var name: String,
    var order: Int
)