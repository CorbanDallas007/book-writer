import SwiftUI

struct NoteEditorView: View {
    @ObservedObject var viewModel: NotesViewModel
    let note: Note
    @Environment(\.modelContext) private var modelContext
    @Environment(\.dismiss) private var dismiss

    @State private var showOriginal: Bool = false
    @State private var showStyleSelector: Bool = false
    @State private var selectedStyle: String = "literal"

    var body: some View {
        NavigationStack {
            VStack(spacing: 0) {
                // Переключатель Оригинал / Исправлено ИИ
                Picker("Режим просмотра", selection: $showOriginal) {
                    Text("Исправлено ИИ").tag(false)
                    Text("Оригинал").tag(true)
                }
                .pickerStyle(.segmented)
                .padding()

                // Редактируемый текст
                TextEditor(text: bindingText)
                    .font(.body)
                    .padding()

                // StyleSelectorView
                if showStyleSelector {
                    StyleSelectorView(selectedStyle: $selectedStyle)
                }

                // Кнопка привязки к главе (заглушка для SwiftData связи)
                Button(action: {
                    showStyleSelector.toggle()
                }) {
                    Text("Привязать к главе / Стили")
                        .font(.subheadline)
                        .foregroundColor(.blue)
                }
                .padding(.bottom)

                Spacer()
            }
            .navigationTitle(note.title)
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .navigationBarTrailing) {
                    ShareLink(
                        item: TextEditorContent(text: showOriginal ? note.rawText : note.correctedText),
                        preview: SharePreview("\(note.title).txt")
                    )
                }
            }
        }
    }

    private var bindingText: Binding<String> {
        Binding(
            get: { showOriginal ? note.rawText : note.correctedText },
            set: { newValue in
                if showOriginal {
                    note.rawText = newValue
                } else {
                    note.correctedText = newValue
                }
                try? modelContext.save()
            }
        )
    }
}

// MARK: - ShareLink content

struct TextEditorContent: Transferable {
    let text: String

    static var transferRepresentation: some TransferRepresentation {
        TextRepresentation(text)
    }
}