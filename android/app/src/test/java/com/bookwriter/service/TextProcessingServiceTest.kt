package com.bookwriter.service

import com.bookwriter.model.StoryContext
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TextProcessingServiceTest {

    private val service = TextProcessingService()

    private val loreContext = listOf(
        StoryContext(keyword = "Элронд", details = "Персонаж, эльф"),
        StoryContext(keyword = "Таинственный Дракон", details = "Локация, таверна")
    )

    @Test
    fun cleanText_removesFillerWords() {
        // Arrange
        val text = "ну короче ГГ зашел в таверну типа"

        // Act
        val result = service.cleanText(text, emptyList(), "literal")

        // Assert
        assertTrue("Should remove 'ну'", !result.contains(" ну ") && !result.startsWith("ну "))
        assertTrue("Should remove 'короче'", !result.contains(" короче "))
        assertTrue("Should remove 'типа'", !result.contains(" типа "))
        assertTrue("Should keep 'ГГ'", result.contains("ГГ"))
        assertTrue("Should keep 'таверну'", result.contains("таверну"))
    }

    @Test
    fun cleanText_fixesProperNamesFromLore() {
        // Arrange
        val text = "элронд зашел в таинственный дракон"

        // Act
        val result = service.cleanText(text, loreContext, "literal")

        // Assert
        assertTrue("Should capitalize 'Элронд'", result.contains("Элронд"))
        assertTrue("Should not contain lowercase 'элронд'", !result.contains("элронд"))
        assertTrue("Should capitalize 'Таинственный Дракон'", result.contains("Таинственный Дракон"))
    }

    @Test
    fun cleanText_fixesSentenceCasing() {
        // Arrange
        val text = "привет мир. как дела? хорошо."

        // Act
        val result = service.cleanText(text, emptyList(), "literal")

        // Assert
        assertTrue("Should capitalize first letter", result.startsWith("Привет"))
        assertTrue("Should capitalize after period", result.contains(" Как"))
        assertTrue("Should capitalize after question mark", result.contains(" Хорошо"))
    }

    @Test
    fun cleanText_literalStyle_returnsProcessedText() {
        // Arrange
        val text = "ну привет мир."

        // Act
        val result = service.cleanText(text, emptyList(), "literal")

        // Assert
        assertEquals("Привет мир.", result)
    }

    @Test
    fun cleanText_verbatimStyle_returnsProcessedText() {
        // Arrange
        val text = "ну привет мир."

        // Act
        val result = service.cleanText(text, emptyList(), "verbatim")

        // Assert
        assertEquals("Привет мир.", result)
    }

    @Test
    fun cleanText_draftStyle_formatsWithBullets() {
        // Arrange
        val text = "Первое предложение. Второе предложение."

        // Act
        val result = service.cleanText(text, emptyList(), "draft")

        // Assert
        assertTrue("Should start with bullet", result.startsWith("• "))
        assertTrue("Should contain second bullet", result.contains("• Второе"))
        assertTrue("Should have newline between bullets", result.contains("\n"))
    }

    @Test
    fun cleanText_emptyText_returnsEmpty() {
        // Arrange
        val text = ""

        // Act
        val result = service.cleanText(text, emptyList(), "literal")

        // Assert
        assertEquals("", result)
    }

    @Test
    fun cleanText_unknownStyle_returnsProcessedText() {
        // Arrange
        val text = "ну привет мир."

        // Act
        val result = service.cleanText(text, emptyList(), "unknown")

        // Assert
        assertEquals("Привет мир.", result)
    }

    @Test
    fun cleanText_combinedAllFeatures() {
        // Arrange
        val text = "ну элронд зашел в таинственный дракон. типа это было круто."

        // Act
        val result = service.cleanText(text, loreContext, "draft")

        // Assert
        assertTrue("Should remove filler words", !result.contains("ну") && !result.contains("типа"))
        assertTrue("Should fix proper names", result.contains("Элронд") && result.contains("Таинственный Дракон"))
        assertTrue("Should format as draft with bullets", result.contains("•"))
    }
}