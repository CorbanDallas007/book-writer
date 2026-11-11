package com.bookwriter.service

import com.bookwriter.model.StoryContext
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.runners.MockitoJUnitRunner
import kotlin.test.assertEquals

@RunWith(MockitoJUnitRunner::class)
class TextProcessingServiceTest {

    private lateinit var service: TextProcessingService

    @Mock
    lateinit var context: StoryContext

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        service = TextProcessingService()
    }

    @Test
    fun `removeFillerWords_removes_common_russian_fillers`() {
        // Arrange
        val text = "ну короче ГГ зашел в типи таверне типа"

        // Act
        val result = service.removeFillerWords(text)

        // Assert
        // "ну", "короче", "типа" should be removed
        assert(result !contains "ну")
        assert(result !contains "короче")
        assert(result !contains "типа")
        assert(result.contains("ГГ"))
        assert(result.contains("таверне"))
    }

    @Test
    fun `fixProperNames_replaces_lore_keywords`() {
        // Arrange
        val text = "элронд и таинственный дракон"
        val context = StoryContext(keyword = "элронд", details = "Персонаж, эльф")

        // Act
        val result = service.fixProperNames(text, listOf(context))

        // Assert
        assert(result.contains("Элронд"))
        assert(!result.contains("элронд")) // Should be capitalized
    }

    @Test
    fun `fixSentenceCapping_capitalizes_first_letter`() {
        // Arrange
        val text = "привет мир. как дела?"

        // Act
        val result = service.fixSentenceCasing(text)

        // Assert
        assert(result.startsWith("Привет"))
        assert(result.contains("Как")) // After question mark
    }

    @Test
    fun `formatAsDraft_formats_with_bullets`() {
        // Arrange
        val text = "Первое предложение. Второе предложение."

        // Act
        val result = service.formatAsDraft(text)

        // Assert
        assert(result.startsWith("• "))
        assert(result.contains("• Второе"))
    }
}