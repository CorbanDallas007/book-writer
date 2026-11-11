# СИСТЕМНОЕ ТЕХНИЧЕСКОЕ ЗАДАНИЕ: iOS-приложение "book-writer"
**Целевая ОС:** iOS 17.0+  
**Стек технологий:** Swift, SwiftUI  
**Локальное хранение:** SwiftData  
**Движки автоматизации:** Apple Speech Framework (SFSpeechRecognizer) и Apple NaturalLanguage Framework

---

## 🎯 АРХИТЕКТУРНЫЙ МАНДАТ
Ты должен полностью сгенерировать все необходимые Swift-файлы для приложения "book-writer" строго в соответствии с описанной ниже структурой. Код должен компилироваться без предупреждений и ошибок, использовать современный синтаксис async/await, изменять состояние интерфейса строго на `@MainActor` и содержать обработку ошибок. Весь пользовательский интерфейс (текст, кнопки, алерты) должен быть на **русском языке**. Никогда не пиши заглушки (комментарии вида `// TODO`) — пиши рабочий код сразу.

---

## 📁 ЦЕЛЕВАЯ СТРУКТУРА КАТАЛОГОВ
Создавай файлы строго по следующим относительным путям внутри директории `book-writer/book-writer/`:
```text
book-writer/
└── book-writer/
    ├── App/
    │   └── book_writerApp.swift
    ├── Models/
    │   ├── Note.swift
    │   ├── Chapter.swift
    │   └── StoryContext.swift
    ├── Services/
    │   ├── SpeechToTextService.swift
    │   └── TextProcessingService.swift
    ├── ViewModels/
    │   ├── AudioRecorderViewModel.swift
    │   ├── NotesViewModel.swift
    │   └── BookTreeViewModel.swift
    └── Views/
        ├── Main/
        │   └── MainTabView.swift
        ├── Record/
        │   └── VoiceRecordView.swift
        ├── Editor/
        │   ├── NoteEditorView.swift
        │   └── StyleSelectorView.swift
        └── BookTree/
            └── BookStructureView.swift
```

---

## 🏗️ ШАГ 1: СЛОЙ ДАННЫХ И ХРАНЕНИЯ (Models)

### 1. `Models/Note.swift`
Создай модель SwiftData для хранения надиктованных фрагментов или глав.
- Поля: `id: UUID`, `title: String`, `rawText: String`, `correctedText: String`, `createdAt: Date`, `chapterId: UUID?`, `processingStyle: String`.
- Реализуй стандартный инициализатор с дефолтными значениями (`title: "Новая запись"`, `processingStyle: "literal"`).

### 2. `Models/Chapter.swift`
Создай модель SwiftData для хранения структуры книги (глав).
- Поля: `id: UUID`, `name: String`, `order: Int`.
- Инициализатор для создания главы с указанием имени и порядкового номера.

### 3. `Models/StoryContext.swift`
Создай модель SwiftData для словаря лора книги (имена персонажей, названия локаций), который ИИ-сервис будет использовать как эталон для исправления специфических слов.
- Поля: `id: UUID`, `keyword: String`, `details: String`.

---

## ⚙️ ШАГ 2: СЛУЖЕБНЫЕ СЕРВИСЫ (Services)

### 1. `Services/SpeechToTextService.swift`
Реализуй движок распознавания речи в реальном времени на базе `SFSpeechRecognizer` и `AVAudioEngine`.
- **Требования:**
    - Напиши явные проверки прав доступа для `SFSpeechRecognizer.requestAuthorization` и `AVAudioSession.sharedInstance().requestRecordPermission`.
    - Локаль: Явно инициализируй `SFSpeechRecognizer(locale: Locale(identifier: "ru-RU"))`.
    - Свойства для отслеживания (через `@Published` или `@Observable`): `isRecording: Bool`, `transcribedText: String`, `errorMessage: String?`.
    - Логика: Привяжи шину аудиодвижка (`AVAudioEngine`), корректно обрабатывай прерывания сессии записи и освобождай буферы памяти при остановке задачи (`SFSpeechAudioBufferRecognitionTask`).

### 2. `Services/TextProcessingService.swift`
Реализуй автономный сервис обработки текста на базе нативного фреймворка Apple **`NaturalLanguage`** (чтобы приложение оставалось полностью легковесным и оффлайновым).
- **Требования:**
    - Функция `cleanText(_ text: String, context: [StoryContext], style: String) -> String`.
    - Используй `NLTokenizer` или `NLTagger` для анализа структуры предложений.
    - Напиши правила автоматического удаления слов-паразитов из надиктованного аудио (например: "ну", "короче", "типа", "в общем", "как бы").
    - Реализуй автоматическое исправление регистра (первая буква предложения — заглавная) и сверку имен собственных со списком ключевых слов из `StoryContext`, чтобы приложение не коверкало имена героев.
    - Реализуй поддержку трех стилей (`style`): `"literal"` (чистая художественная речь), `"verbatim"` (дословный текст как есть с исправленной орфографией), и `"draft"` (автоматическое форматирование в виде тезисов/списка).

---

## 🖥️ ШАГ 3: УПРАВЛЕНИЕ И ИНТЕРФЕЙС (ViewModels & Views)

### 1. `Views/Record/VoiceRecordView.swift` & `ViewModels/AudioRecorderViewModel.swift`
- **Интерфейс:** Минималистичный экран, в центре которого находится большая кнопка записи микрофона.
- **Особенности:** Добавь визуальную анимацию пульсации или звуковой волны во время записи, привязанную к `SpeechToTextService.isRecording`. Выводи текст, распознаваемый в реальном времени, в специальный скроллящийся блок.
- **Действия:** Кнопка «Остановить и Сохранить» автоматически отправляет текст в `TextProcessingService`, создает новую запись `Note`, сохраняет её в контекст базы данных SwiftData и сбрасывает буфер.

### 2. `Views/Editor/NoteEditorView.swift` & `ViewModels/NotesViewModel.swift`
- **Интерфейс:** Экран детального просмотра заметки с переключателем (`Picker`) вверху: «Оригинал» и «Исправлено ИИ».
- **Особенности:** Полноэкранный редактируемый `TextEditor`. В навигационной панели добавь кнопку привязки этой заметки к определенной главе (`Chapter`). Подключи `StyleSelectorView` для возможности повторной очистки текста в другом стиле.
- **Экспорт:** Реализуй системную кнопку «Поделиться» (`ShareLink`), позволяющую мгновенно экспортировать текст заметки в формате `.txt` (для передачи на ПК или бэкапа).

### 3. `Views/BookTree/BookStructureView.swift` & `ViewModels/BookTreeViewModel.swift`
- **Интерфейс:** Дерево глав и оглавление книги.
- **Особенности:** Вывод всех сущностей `Chapter`, отсортированных по полю `.order`. Каждая глава отображается как раскрывающаяся секция, внутри которой находится список относящихся к ней заметок (`Note`).
- **Действия:** Кнопка «Добавить главу» (вызывает диалоговое окно ввода имени) и поддержка жеста удаления свайпом (`onDelete`) для заметок и глав.

### 4. `Views/Main/MainTabView.swift`
- **Интерфейс:** Главный контейнер приложения на базе `TabView`.
- **Вкладки:**
    1. `VoiceRecordView` — Иконка: `mic.circle.fill`, Текст: "Запись".
    2. `BookStructureView` — Иконка: `book.closed.fill`, Текст: "Книга".
    3. Экран лора/словаря — Простой список элементов `StoryContext`, где автор может через кнопку плюса добавлять имена героев и описание к ним, чтобы ИИ понимал контекст книги.

---

## 🏁 ШАГ 4: ТОЧКА ВХОДА

### `App/book_writerApp.swift`
Безопасно инициализируй приложение. Создай `sharedModelContainer` для моделей `Note.self`, `Chapter.self` и `StoryContext.self`. Внедрить контейнер через модификатор `.modelContainer(sharedModelContainer)` на корневой вью `MainTabView()`.

---

## 🤖 ЧЕК-ЛИСТ ДЛЯ CLINE
1. Пиши **полный, рабочий код** во всех файлах, без пропусков алгоритмов и заглушек.
2. Обрабатывай ошибки во всех ключевых точках (`do-catch` при старте аудио, проверке разрешений, сохранении данных в БД).
3. Все обновления интерфейса и работа со SwiftData должны строго происходить на главном потоке (`@MainActor`).
4. Выполняй задачу пошагово: сначала создай слой моделей данных, затем сервисы обработки, логику ViewModels и в конце свяжи всё визуальным интерфейсом.
