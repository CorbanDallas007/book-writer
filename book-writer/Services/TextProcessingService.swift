import Foundation
import NaturalLanguage

@MainActor
final class TextProcessingService {

    private let fillerWords: Set<String> = ["ну", "короче", "типа", "в общем", "как бы"]

    func cleanText(_ text: String, context: [StoryContext], style: String) -> String {
        var result = text

        // Удаляем слова-паразиты
        result = removeFillerWords(from: result)

        // Исправляем имена собственные из контекста лора
        result = fixProperNames(in: result, context: context)

        // Исправляем регистр (первая буква предложения — заглавная)
        result = fixSentenceCasing(of: result)

        switch style {
        case "literal":
            return result
        case "verbatim":
            return result
        case "draft":
            return formatAsDraft(result)
        default:
            return result
        }
    }

    private func removeFillerWords(from text: String) -> String {
        var words = text.components(separatedBy: .whitespacesAndNewlines)
        words = words.filter { word in
            let trimmed = word.trimmingCharacters(in: .punctuationCharacters)
            return !fillerWords.contains(trimmed.lowercased())
        }
        return words.joined(separator: " ")
    }

    private func fixProperNames(in text: String, context: [StoryContext]) -> String {
        var result = text
        for entry in context {
            let keyword = entry.keyword
            let lowercasedText = result.lowercased()
            let lowercasedKeyword = keyword.lowercased()
            var searchStartIndex = lowercasedText.startIndex

            while let range = lowercasedText.range(of: lowercasedKeyword, range: searchStartIndex..<lowercasedText.endIndex) {
                let start = range.lowerBound
                let end = range.upperBound

                // Проверяем, что это целое слово (не часть другого слова)
                let isWordStart = start == result.startIndex || result.index(before: start) == " " || result.index(before: start) == "\n"
                let isWordEnd = end == result.endIndex || result[end] == " " || result[end] == "\n" || result[end] == "." || result[end] == "," || result[end] == "!" || result[end] == "?" || result[end] == ":"

                if isWordStart && isWordEnd {
                    result.replaceSubrange(start..<end, with: keyword)
                }

                searchStartIndex = lowercasedText.index(after: start)
            }
        }
        return result
    }

    private func fixSentenceCasing(of text: String) -> String {
        guard !text.isEmpty else { return text }

        var result = ""
        var capitalizeNext = true

        for char in text {
            if capitalizeNext && char.isLetter {
                result.append(char.uppercased())
                capitalizeNext = false
            } else {
                result.append(char)
            }

            if char == "." || char == "!" || char == "?" || char == "\n" {
                capitalizeNext = true
            }
        }

        return result
    }

    private func formatAsDraft(_ text: String) -> String {
        let sentences = text.components(separatedBy: CharacterSet(charactersIn: ".!?\n"))
            .map { $0.trimmingCharacters(in: .whitespacesAndNewlines) }
            .filter { !$0.isEmpty }

        let bulletPoints = sentences.map { "• \($0)" }
        return bulletPoints.joined(separator: "\n")
    }
}