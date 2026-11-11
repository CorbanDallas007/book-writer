import SwiftUI

struct StyleSelectorView: View {
    @Binding var selectedStyle: String

    private let styles: [(key: String, title: String)] = [
        ("literal", "Литературный"),
        ("verbatim", "Дословно"),
        ("draft", "Черновик")
    ]

    var body: some View {
        VStack(alignment: .leading, spacing: 12) {
            Text("Стиль обработки")
                .font(.headline)

            Picker("Стиль", selection: $selectedStyle) {
                ForEach(styles, id: \.key) { style in
                    Text(style.title).tag(style.key)
                }
            }
            .pickerStyle(.segmented)
        }
        .padding()
    }
}