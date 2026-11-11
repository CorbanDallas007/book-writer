import Foundation
import SwiftData
import SwiftUI

@MainActor
final class BookTreeViewModel: ObservableObject {
    private let modelContext: ModelContext

    @Published var chapters: [Chapter] = []
    @Published var notes: [Note] = []
    @Published var storyContexts: [StoryContext] = []

    init(modelContext: ModelContext) {
        self.modelContext = modelContext
    }

    func loadData() {
        let chapterDescriptor = FetchDescriptor<Chapter>(sortBy: [SortDescriptor(\.order, order: .forward)])
        let noteDescriptor = FetchDescriptor<Note>(sortBy: [SortDescriptor(\.createdAt, order: .reverse)])
        let contextDescriptor = FetchDescriptor<StoryContext>(sortBy: [SortDescriptor(\.keyword, order: .forward)])

        do {
            chapters = try modelContext.fetch(chapterDescriptor)
            notes = try modelContext.fetch(noteDescriptor)
            storyContexts = try modelContext.fetch(contextDescriptor)
        } catch {
            print("Ошибка загрузки данных дерева книги: \(error.localizedDescription)")
        }
    }

    func addChapter(name: String, order: Int? = nil) {
        let nextOrder = order ?? (chapters.map { $0.order }.max() ?? 0) + 1
        let chapter = Chapter(name: name, order: nextOrder)
        modelContext.insert(chapter)
        do {
            try modelContext.save()
            loadData()
        } catch {
            print("Ошибка добавления главы: \(error.localizedDescription)")
        }
    }

    func deleteChapter(_ chapter: Chapter) {
        modelContext.delete(chapter)
        do {
            try modelContext.save()
            loadData()
        } catch {
            print("Ошибка удаления главы: \(error.localizedDescription)")
        }
    }

    func addStoryContext(keyword: String, details: String) {
        let context = StoryContext(keyword: keyword, details: details)
        modelContext.insert(context)
        do {
            try modelContext.save()
            loadData()
        } catch {
            print("Ошибка добавления лора: \(error.localizedDescription)")
        }
    }

    func deleteStoryContext(_ context: StoryContext) {
        modelContext.delete(context)
        do {
            try modelContext.save()
            loadData()
        } catch {
            print("Ошибка удаления лора: \(error.localizedDescription)")
        }
    }

    func notesForChapter(_ chapter: Chapter) -> [Note] {
        return notes.filter { $0.chapterId == chapter.id }
    }
}