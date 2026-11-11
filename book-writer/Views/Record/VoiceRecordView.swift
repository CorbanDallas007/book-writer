import SwiftUI

struct VoiceRecordView: View {
    @ObservedObject var viewModel: AudioRecorderViewModel
    @Environment(\.colorScheme) private var colorScheme
    @State private var showingPermissionsAlert: Bool = false
    @State private var showSaveButton: Bool = false

    var body: some View {
        NavigationStack {
            VStack(spacing: 24) {
                // Текст распознавания в реальном времени
                ScrollView {
                    Text(viewModel.transcribedText.isEmpty ? "Готов к записи..." : viewModel.transcribedText)
                        .font(.body)
                        .foregroundColor(viewModel.transcribedText.isEmpty ? .secondary : .primary)
                        .frame(maxWidth: .infinity, alignment: .leading)
                        .padding()
                }
                .frame(maxHeight: 300)
                .background(colorScheme == .dark ? Color(.systemGray5).opacity(0.3) : Color(.systemGray6))
                .cornerRadius(12)
                .padding(.horizontal)

                // Кнопка записи
                ZStack {
                    if viewModel.isRecording {
                        PulseAnimation()
                    }

                    Button(action: {
                        if !viewModel.isRecording {
                            Task {
                                let granted = await viewModel.requestPermissions()
                                if granted {
                                    viewModel.startRecording()
                                } else {
                                    showingPermissionsAlert = true
                                }
                            }
                        } else {
                            viewModel.stopAndSave()
                        }
                    }) {
                        Image(systemName: viewModel.isRecording ? "stop.fill" : "mic.fill")
                            .font(.system(size: 50))
                            .foregroundColor(.white)
                            .frame(width: 110, height: 110)
                            .background(viewModel.isRecording ? Color.red : Color.blue)
                            .clipShape(Circle())
                    }
                    .scaleEffect(viewModel.isRecording ? 0.95 : 1.0)
                    .animation(.spring(), value: viewModel.isRecording)
                }

                if let error = viewModel.errorMessage {
                    Text(error)
                        .font(.caption)
                        .foregroundColor(.red)
                        .padding(.horizontal)
                }

                // Кнопка демо-ввода для симулятора
                #if targetEnvironment(simulator)
                Button(action: {
                    viewModel.simulateDemoInput()
                }) {
                    HStack {
                        Image(systemName: "play.circle.fill")
                        Text("Демо-ввод")
                    }
                    .font(.subheadline)
                    .foregroundColor(.blue)
                    .padding(.vertical, 8)
                }
                #endif

                Spacer()
            }
            .navigationTitle("Диктовка")
            .navigationBarTitleDisplayMode(.inline)
            .alert("Нет доступа", isPresented: $showingPermissionsAlert) {
                Button("OK", role: .cancel) { }
            } message: {
                Text("Приложению необходим доступ к микрофону и распознаванию речи для работы диктовки.")
            }
        }
    }
}

// MARK: - Анимация пульсации

struct PulseAnimation: View {
    @State private var animationAmount: CGFloat = 1.0

    var body: some View {
        Circle()
            .stroke(Color.red.opacity(0.4), lineWidth: 4)
            .frame(width: 140, height: 140)
            .scaleEffect(animationAmount)
            .opacity(1 - animationAmount)
            .animation(
                .easeInOut(duration: 1.5)
                .repeatForever(autoreverses: false),
                value: animationAmount
            )
            .onAppear {
                animationAmount = 2.0
            }
    }
}