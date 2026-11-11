import SwiftUI

struct BookStructureView: View {
    @ObservedObject var viewModel: BookTreeViewModel
    @Environment(\.colorScheme) private var colorScheme
    @State private var newChapterName: String = ""
    @State private var showingAddChapterAlert: Bool = false

    var body: some View {
        NavigationStack {
            List {
                ForEach(viewModel.chapters) { chapter in
                    Section {
                        ForEach(viewModel.notesForChapter(chapter)) { note in
                            HStack {
                                VStack(alignment: .leading) {
                                    Text(note.title)
                                        .font(.headline)
                                    Text(note.createdAt, style: .date)
                                        .font(.caption)
                                        .foregroundColor(.secondary)
                                }
                                Spacer()
                                Text(note.processingStyle)
                                    .font(.caption2)
                                    .padding(4)
                                    .background(colorScheme == .dark ? Color.blue.opacity(0.2) : Color.blue.opacity(0.1))
                                    .cornerRadius(4)
                            }
                        }
                        .onDelete { indexSet in
                            deleteNotes(from: chapter, at: indexSet)
                        }
                    } header: {
                        HStack {
                            Text(chapter.name)
                                .font(.headline)
                            Spacer()
                            Text("\(chapter.order)")
                                .font(.caption)
                                .foregroundColor(.secondary)
                        }
                    }
                }
            }
            .navigationTitle("Дерево глав")
            .toolbar {
                ToolbarItem(placement: .navigationBarTrailing) {
                    Button(action: {
                        showingAddChapterAlert = true
                    }) {
                        Image(systemName: "plus")
                    }
                }
            }
            .alert("Новая глава", isPresented: $showingAddChapterAlert) {
                TextField("Название главы", text: $newChapterName)
                Button("Отмена", role: .cancel) { }
                Button("Создать") {
                    viewModel.addChapter(name: newChapterName)
                    newChapterName = ""
                }
            } message: {
                Text("Введите название новой главы")
            }
        }
    }

    private func deleteNotes(from chapter: Chapter, at offsets: IndexSet) {
        let chapterNotes = viewModel.notesForChapter(chapter)
        for index in offsets {
            viewModel.modelContext.delete(chapterNotes[index])
        }
        do {
            try viewModel.modelContext.save()
            viewModel.loadData()
        } catch {
            print("Ошибка удаления заметки: \(error.localizedDescription)")
        }
    }
}