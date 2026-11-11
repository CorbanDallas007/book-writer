import Foundation
import SwiftUI

@MainActor
final class AudioRecorderViewModel: ObservableObject {
    private let speechService: SpeechToTextService
    private let textProcessingService: TextProcessingService
    private let modelContext: ModelContext

    @Published var isRecording: Bool = false
    @Published var transcribedText: String = ""
    @Published var errorMessage: String? = nil
    @Published var notes: [Note] = []

    init(speechService: SpeechToTextService, textProcessingService: TextProcessingService, modelContext: ModelContext) {
        self.speechService = speechService
        self.textProcessingService = textProcessingService
        self.modelContext = modelContext
    }

    func requestPermissions() async -> Bool {
        let granted = await speechService.requestPermissions()
        if !granted {
            errorMessage = "Нет доступа к микрофону или распознаванию речи"
        }
        return granted
    }

    func toggleRecording() {
        if speechService.isRecording {
            stopAndSave()
        } else {
            startRecording()
        }
    }

    private func startRecording() {
        speechService.startRecording()
        isRecording = speechService.isRecording
        transcribedText = speechService.transcribedText
        errorMessage = speechService.errorMessage
    }

    private func stopAndSave() {
        speechService.stopRecording()
        isRecording = speechService.isRecording

        let rawText = transcribedText
        let context: [StoryContext] = []
        let correctedText = textProcessingService.cleanText(rawText, context: context, style: "literal")

        print("Очистка текста:")
        print("Было: \(rawText)")
        print("Стало: \(correctedText)")

        let note = Note(
            title: "Запись \(formattedDate())",
            rawText: rawText,
            correctedText: correctedText,
            createdAt: Date(),
            processingStyle: "literal"
        )

        modelContext.insert(note)
        do {
            try modelContext.save()
            notes.append(note)
        } catch {
            errorMessage = "Ошибка сохранения заметки: \(error.localizedDescription)"
        }

        transcribedText = ""
    }

    func simulateDemoInput() {
        let demoText = "ну короче ГГ зашел в таверну типа Таинственный Дракон и там сидел этот как бы эльф Элронд"
        speechService.simulateVoiceInput(text: demoText)
        transcribedText = speechService.transcribedText
    }

    private func formattedDate() -> String {
        let formatter = DateFormatter()
        formatter.dateFormat = "dd.MM HH:mm"
        formatter.locale = Locale(identifier: "ru_RU")
        return formatter.string(from: Date())
    }
}