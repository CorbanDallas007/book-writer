import Foundation
import SwiftData
import SwiftUI

@MainActor
final class NotesViewModel: ObservableObject {
    private let modelContext: ModelContext
    private let textProcessingService: TextProcessingService

    @Published var notes: [Note] = []

    init(modelContext: ModelContext, textProcessingService: TextProcessingService) {
        self.modelContext = modelContext
        self.textProcessingService = textProcessingService
    }

    func loadNotes() {
        let descriptor = FetchDescriptor<Note>(sortBy: [SortDescriptor(\.createdAt, order: .reverse)])
        do {
            notes = try modelContext.fetch(descriptor)
        } catch {
            print("Ошибка загрузки заметок: \(error.localizedDescription)")
        }
    }

    func saveNote(_ note: Note) {
        do {
            try modelContext.save()
            loadNotes()
        } catch {
            print("Ошибка сохранения заметки: \(error.localizedDescription)")
        }
    }

    func deleteNote(_ note: Note) {
        modelContext.delete(note)
        do {
            try modelContext.save()
            loadNotes()
        } catch {
            print("Ошибка удаления заметки: \(error.localizedDescription)")
        }
    }

    func reprocessNote(_ note: Note, style: String) {
        let context: [StoryContext] = []
        let corrected = textProcessingService.cleanText(note.rawText, context: context, style: style)

        print("Переобработка заметки \(note.title):")
        print("Было: \(note.rawText)")
        print("Стало: \(corrected)")

        note.correctedText = corrected
        note.processingStyle = style
        saveNote(note)
    }
}