import SwiftData
import Foundation

@Model
final class Note {
    var id: UUID
    var title: String
    var rawText: String
    var correctedText: String
    var createdAt: Date
    var chapterId: UUID?
    var processingStyle: String

    init(
        id: UUID = UUID(),
        title: String = "Новая запись",
        rawText: String = "",
        correctedText: String = "",
        createdAt: Date = Date(),
        chapterId: UUID? = nil,
        processingStyle: String = "literal"
    ) {
        self.id = id
        self.title = title
        self.rawText = rawText
        self.correctedText = correctedText
        self.createdAt = createdAt
        self.chapterId = chapterId
        self.processingStyle = processingStyle
    }
}