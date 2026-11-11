import SwiftUI
import SwiftData

struct MainTabView: View {
    @Environment(\.modelContext) private var modelContext
    @State private var bookTreeVM: BookTreeViewModel?
    @State private var notesVM: NotesViewModel?
    @State private var audioRecorderVM: AudioRecorderViewModel?

    var body: some View {
        Group {
            if let bookTreeVM, let notesVM, let audioRecorderVM {
                TabView {
                    VoiceRecordView(viewModel: audioRecorderVM)
                        .tabItem {
                            Image(systemName: "mic.circle.fill")
                            Text("Запись")
                        }

                    BookStructureView(viewModel: bookTreeVM)
                        .tabItem {
                            Image(systemName: "book.closed.fill")
                            Text("Книга")
                        }

                    StoryContextListView(viewModel: bookTreeVM)
                        .tabItem {
                            Image(systemName: "character.book.closed.fill")
                            Text("Лор")
                        }
                }
                .onAppear {
                    bookTreeVM.loadData()
                    notesVM.loadNotes()
                }
            }
        }
        .onAppear {
            let speechService = SpeechToTextService()
            let textProcessingService = TextProcessingService()
            self.audioRecorderVM = AudioRecorderViewModel(
                speechService: speechService,
                textProcessingService: textProcessingService,
                modelContext: modelContext
            )
            self.notesVM = NotesViewModel(
                modelContext: modelContext,
                textProcessingService: textProcessingService
            )
            self.bookTreeVM = BookTreeViewModel(modelContext: modelContext)
        }
    }
}

// MARK: - Лор / Словарь контекста

struct StoryContextListView: View {
    @ObservedObject var viewModel: BookTreeViewModel
    @State private var newKeyword: String = ""
    @State private var newDetails: String = ""

    var body: some View {
        NavigationStack {
            List {
                ForEach(viewModel.storyContexts) { context in
                    VStack(alignment: .leading) {
                        Text(context.keyword)
                            .font(.headline)
                        if !context.details.isEmpty {
                            Text(context.details)
                                .font(.caption)
                                .foregroundColor(.secondary)
                        }
                    }
                }
                .onDelete(perform: deleteContext)
            }
            .navigationTitle("Лор книги")
            .toolbar {
                ToolbarItem(placement: .navigationBarTrailing) {
                    Button("Добавить") {
                        addContext()
                    }
                }
            }
        }
    }

    private func addContext() {
        guard !newKeyword.isEmpty else { return }
        viewModel.addStoryContext(keyword: newKeyword, details: newDetails)
        newKeyword = ""
        newDetails = ""
    }

    private func deleteContext(at offsets: IndexSet) {
        for index in offsets {
            viewModel.deleteStoryContext(viewModel.storyContexts[index])
        }
    }
}