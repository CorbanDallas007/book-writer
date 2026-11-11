import SwiftData
import Foundation

@Model
final class StoryContext {
    var id: UUID
    var keyword: String
    var details: String

    init(id: UUID = UUID(), keyword: String, details: String = "") {
        self.id = id
        self.keyword = keyword
        self.details = details
    }
}