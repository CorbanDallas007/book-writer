package com.bookwriter.service

import com.bookwriter.model.StoryContext

class TextProcessingService {

    private val fillerWords = setOf("ну", "короче", "типа", "в общем", "как бы")

    fun cleanText(text: String, context: List<StoryContext>, style: String): String {
        var result = text

        // Удаляем слова-паразиты
        result = removeFillerWords(result)

        // Исправляем имена собственные из контекста лора
        result = fixProperNames(result, context)

        // Исправляем регистр (первая буква предложения — заглавная)
        result = fixSentenceCasing(result)

        return when (style) {
            "literal" -> result
            "verbatim" -> result
            "draft" -> formatAsDraft(result)
            else -> result
        }
    }

    private fun removeFillerWords(text: String): String {
        return text.split("\\s+".toRegex())
            .filter { word ->
                val trimmed = word.trim().replace(Regex("[.,!?;:()«»\"']"), "")
                !fillerWords.contains(trimmed.lowercase())
            }
            .joinToString(" ")
    }

    private fun fixProperNames(text: String, context: List<StoryContext>): String {
        var result = text
        for (entry in context) {
            val keyword = entry.keyword
            val lowercasedText = result.lowercase()
            val lowercasedKeyword = keyword.lowercase()
            var searchStartIndex = 0

            while (true) {
                val index = lowercasedText.indexOf(lowercasedKeyword, searchStartIndex)
                if (index == -1) break

                val start = index
                val end = index + lowercasedKeyword.length

                // Проверяем, что это целое слово (не часть другого слова)
                val isWordStart = start == 0 || lowercasedText[start - 1] == ' ' || lowercasedText[start - 1] == '\n'
                val isWordEnd = end == lowercasedText.length ||
                    lowercasedText[end] == ' ' ||
                    lowercasedText[end] == '\n' ||
                    lowercasedText[end] == '.' ||
                    lowercasedText[end] == ',' ||
                    lowercasedText[end] == '!' ||
                    lowercasedText[end] == '?' ||
                    lowercasedText[end] == ':'

                if (isWordStart && isWordEnd) {
                    result = result.replaceRange(start, end, keyword)
                }

                searchStartIndex = start + 1
            }
        }
        return result
    }

    private fun fixSentenceCasing(text: String): String {
        if (text.isEmpty()) return text

        val result = StringBuilder()
        var capitalizeNext = true

        for (char in text) {
            if (capitalizeNext && char.isLetter()) {
                result.append(char.uppercaseChar())
                capitalizeNext = false
            } else {
                result.append(char)
            }

            if (char == '.' || char == '!' || char == '?' || char == '\n') {
                capitalizeNext = true
            }
        }

        return result.toString()
    }

    private fun formatAsDraft(text: String): String {
        val sentences = text.split(Regex("[.!?\\n]"))
            .map { it.trim() }
            .filter { it.isNotEmpty() }

        return sentences.joinToString("\n") { "• $it" }
    }

    private fun String.replaceRange(start: Int, end: Int, replacement: String): String {
        return this.substring(0, start) + replacement + this.substring(end)
    }
}