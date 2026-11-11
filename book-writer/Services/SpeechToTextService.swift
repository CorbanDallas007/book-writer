import Foundation
import AVFoundation
import Speech

@MainActor
final class SpeechToTextService: NSObject, ObservableObject {
    @Published var isRecording: Bool = false
    @Published var transcribedText: String = ""
    @Published var errorMessage: String? = nil

    private let speechRecognizer: SFSpeechRecognizer?
    private var recognitionTask: SFSpeechAudioBufferRecognitionTask?
    private var audioEngine: AVAudioEngine?

    private var simulateTimer: Timer? = nil

    override init() {
        self.speechRecognizer = SFSpeechRecognizer(locale: Locale(identifier: "ru-RU"))
        super.init()
    }

    func requestPermissions() async -> Bool {
        let speechAuth = await withCheckedContinuation { continuation in
            SFSpeechRecognizer.requestAuthorization { status in
                continuation.resume(returning: status == .authorized)
            }
        }

        let micAuth = await withCheckedContinuation { continuation in
            AVAudioSession.sharedInstance().requestRecordPermission { granted in
                continuation.resume(returning: granted)
            }
        }

        return speechAuth && micAuth
    }

    func startRecording() {
        #if targetEnvironment(simulator)
        isRecording = true
        transcribedText = ""
        errorMessage = nil
        return
        #else
        do {
            try startAudioEngine()
            isRecording = true
            transcribedText = ""
            errorMessage = nil
        } catch {
            errorMessage = "Ошибка запуска записи: \(error.localizedDescription)"
            isRecording = false
        }
        #endif
    }

    func stopRecording() {
        #if targetEnvironment(simulator)
        isRecording = false
        simulateTimer?.invalidate()
        simulateTimer = nil
        return
        #else
        audioEngine?.stop()
        audioEngine?.inputNode.removeTap(onBus: 0)
        recognitionTask?.cancel()
        recognitionTask = nil
        audioEngine = nil
        isRecording = false
        #endif
    }

    private func startAudioEngine() throws {
        guard let speechRecognizer = speechRecognizer, speechRecognizer.isAvailable else {
            throw NSError(domain: "SpeechToTextService", code: 1, userInfo: [NSLocalizedDescriptionKey: "Распознаватель речи недоступен"])
        }

        let audioSession = AVAudioSession.sharedInstance()
        try audioSession.setCategory(.record, mode: .measurement, options: .duckOthers)
        try audioSession.setActive(true, options: .notifyOthersOnDeactivation)

        let recognitionRequest = SFSpeechAudioBufferRecognitionRequest()
        recognitionRequest.shouldReportPartialResults = true

        let audioEngine = AVAudioEngine()
        self.audioEngine = audioEngine

        let inputNode = audioEngine.inputNode
        inputNode.installTap(onBus: 0, bufferSize: 1024, format: inputNode.outputFormat(forBus: 0)) { [weak self] buffer, _ in
            recognitionRequest.append(buffer)
        }

        audioEngine.prepare()
        try audioEngine.start()

        let task = speechRecognizer.recognitionTask(with: recognitionRequest) { [weak self] result, error in
            Task { @MainActor in
                if let result = result {
                    self?.transcribedText = result.bestTranscription.formattedString
                }
                if let error = error {
                    self?.errorMessage = "Ошибка распознавания: \(error.localizedDescription)"
                    self?.stopRecording()
                }
            }
        }

        self.recognitionTask = task
    }

    // MARK: - Simulator Mode

    func simulateVoiceInput(text: String) {
        #if targetEnvironment(simulator)
        transcribedText = ""
        var words = text.components(separatedBy: " ")
        var index = 0

        simulateTimer = Timer.scheduledTimer(withTimeInterval: 0.5, repeats: true) { [weak self] timer in
            guard let self = self else {
                timer.invalidate()
                return
            }

            if index < words.count {
                let word = words[index]
                if self.transcribedText.isEmpty {
                    self.transcribedText = word
                } else {
                    self.transcribedText = self.transcribedText + " " + word
                }
                index += 1
            } else {
                timer.invalidate()
                self.simulateTimer = nil
            }
        }
        #endif
    }
}