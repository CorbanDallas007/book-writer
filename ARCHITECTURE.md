# 🏗️ ARCHITECTURE.md — Техническая архитектура проекта book-writer

## 1. Обзор

**book-writer** — автономное Android-приложение для писателей, которое превращает устную речь в структурированный текст офлайн. Весь pipeline распознавания речи работает локально на устройстве без подключения к интернету.

## 2. Диаграмма архитектуры (слои)

```
┌─────────────────────────────────────────────────────────────────┐
│                         UI Layer (Jetpack Compose)               │
│  ┌─────────────┐  ┌─────────────┐  ┌───────────────────────┐   │
│  │ RecordScreen│  │  BookScreen │  │    LoreScreen         │   │
│  └──────┬──────┘  └──────┬──────┘  └──────────┬────────────┘   │
└─────────┼────────────────┼────────────────────┼─────────────────┘
          │                │                    │
          ▼                ▼                    ▼
┌─────────────────────────────────────────────────────────────────┐
│                      ViewModel Layer (MVVM)                      │
│  ┌─────────────────┐      ┌─────────────────────────────────┐   │
│  │ NotesViewModel  │      │    BookTreeViewModel            │   │
│  │ (LiveData<List< │      │  (chapters, notes, storyContext │   │
│  │  Note>>)        │      │   lists in-memory)              │   │
│  └────────┬────────┘      └──────────────┬──────────────────┘   │
└───────────┼──────────────────────────────┼──────────────────────┘
            │                              │
            ▼                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                    Service & Data Layer                          │
│  ┌──────────────────┐      ┌───────────────────────────────┐   │
│  │ WhisperASRService│      │      Room Database            │   │
│  │  (AudioRecord +  │      │  ┌─────────────────────────┐  │   │
│  │   JNI bridge)    │      │  │ ChapterEntity           │  │   │
│  └────────┬─────────┘      │  │ NoteEntity              │  │   │
└───────────┼────────────────┘  │  │ StoryContextEntity    │  │   │
            │                   │  └─────────────────────────┘  │   │
            ▼                   │       DAO Layer             │   │
┌───────────────────────────────┐  │  (ChapterDao, NoteDao,  │   │
│      JNI / Native Layer       │  │   StoryContextDao)      │   │
│  ┌─────────────────────────┐  │  └─────────────────────────┘   │
│  │ whisper_jni.cpp          │  │                                │   │
│  │ (initModel, recognize,  │  │                                │   │
│  │   freeModel)             │  │                                │   │
│  └───────────┬─────────────┘  │                                │   │
└──────────────┼────────────────┘                                │
               ▼                                                 │
┌─────────────────────────────────────────────────────────────────┐
│              C++ Native Layer (whisper.cpp + GGML)              │
│  ┌───────────────────────────────────────────────────────────┐  │
│  │ libwhisper.so (SHARED, CMake + Android NDK)               │  │
│  │  ├── whisper.cpp   — основной движок распознавания       │  │
│  │  ├── whisper.h     — публичный API (whisper_full, etc.)  │  │
│  │  ├── ggml.c/h      — тензорная библиотека                │  │
│  │  └── whisper_src/  — исходники whisper.cpp                │  │
│  └───────────────────────────────────────────────────────────┘  │

## 3. Слои архитектуры

### 3.1 UI Layer

- **Технология:** Jetpack Compose (Material Design 3)
- **Навигация:** `NavHost` с тремя destinations: `record`, `book`, `lore`
- **Экраны:**
  - `RecordScreen` — запись аудио, отображение распознанного текста, сохранение заметки
  - `BookScreen` — дерево глав, привязка заметок к главам
  - `LoreScreen` — локальный словарь лора (персонажи, локации)
- **Нижняя навигация:** `BottomNavigation` с 3 табами (Микрофон, Книга, Лор)
- **Сборка ViewModel:** через `hiltViewModel` / `viewModel { }` (в `MainActivity`)

### 3.2 ViewModel Layer (MVVM)

| ViewModel | Ответственность | Состояние |
|-----------|----------------|-----------|
| `NotesViewModel` | CRUD заметок, reprocessing, загрузка демо-заметок | `MutableLiveData<List<Note>>`, `selectedNote` |
| `BookTreeViewModel` | Управление главами, story-контекстами, фильтрация заметок по главе | in-memory `MutableList<Chapter>`, `MutableList<StoryContext>` |

- **Threading:** `viewModelScope.launch(Dispatchers.IO)` для операций с БД
- **LiveData:** `_notes.postValue(...)` для обновления UI из фонового потока

### 3.3 Service Layer

#### `WhisperASRService`
- **Назначение:** фоновый сервис для записи аудио и распознавания речи через Whisper
- **Аудио:** `AudioRecord` (PCM 16-bit, 16000 Hz, Mono)
- **Модель:** `ggml-tiny.bin` копируется из `assets/` в `context.filesDir/` при инициализации
- **JNI вызовы:** `WhisperJNI_initModel`, `WhisperJNI_recognize`, `WhisperJNI_freeModel`
- **Конвертация аудио:** `ShortArray` → `FloatArray` (нормализация `/ 32768f`, диапазон -1.0…1.0)

#### `TextProcessingService`
- **Назначение:** постобработка распознанного текста
- **Функции:** удаление слов-паразитов, исправление имён из контекста лора, исправление регистра, форматирование черновика (буллиты)

### 3.4 Data Layer (Room Database)

**База:** `BookDatabase` (Room, версия 1, имя `book_database`)

| Таблица | Сущность | Ключевые поля |
|---------|----------|---------------|
| `chapters` | `ChapterEntity` | `id` (PK), `name`, `order` |
| `notes` | `NoteEntity` | `id` (PK), `title`, `raw_text`, `corrected_text`, `created_at`, `chapter_id` (FK), `processing_style` |
| `story_context` | `StoryContextEntity` | `id` (PK), `keyword`, `details` |

**DAOs:** `ChapterDao`, `NoteDao`, `StoryContextDao` — все операции `suspend` (корутины)

### 3.5 JNI / Native Layer

**CMake цель:** `libwhisper.so` (SHARED)

**Файлы:**
- `android/app/src/main/cpp/CMakeLists.txt` — конфигурация сборки (C++17, оптимизация -O3, LTO)
- `android/app/src/main/cpp/whisper_jni.cpp` — JNI-мост
- `android/app/src/main/cpp/whisper_src/` — исходники whisper.cpp и GGML
- `android/app/src/main/cpp/whisper_src/include/whisper.h` — обёртка-заголовок для JNI

**JNI-интерфейс (Kotlin → C++):**

| Kotlin метод | C++ функция | Описание |
|-------------|-------------|----------|
| `WhisperJNI_initModel(modelPath: String): Long` | `Java_com_bookwriter_WhisperASRService_initModel` | Загрузка модели, возвращает указатель `whisper_context*` как `jlong` |
| `WhisperJNI_recognize(ctx: Long, audioData: FloatArray, sampleRate: Int): String` | `Java_com_bookwriter_WhisperASRService_recognize` | Распознавание аудио, возвращает строку текста |
| `WhisperJNI_freeModel(ctx: Long)` | `Java_com_bookwriter_WhisperASRService_freeModel` | Освобождение ресурсов модели |

**Параметры `whisper_full_params`:**
- `strategy = WHISPER_SAMPLING_GREEDY`
- `print_special = false`, `print_progress = false`, `print_timestamps = false`
- `max_len = 4096`

### 3.6 Модель данных (Kotlin)

| Класс | Расположение | Назначение |
|-------|-------------|------------|
| `Note` | `model/Note.kt` | Модель заметки (id, title, rawText, correctedText, createdAt, chapterId, processingStyle) |
| `Chapter` | `model/Chapter.kt` | Модель главы (id, name, order) |
| `StoryContext` | `model/StoryContext.kt` | Контекст лора (id, keyword, details) |

## 4. Поток данных (Data Flow)

### Запись и распознавание речи:

1. Пользователь нажимает «Старт записи» на `RecordScreen`
2. `WhisperASRService` инициализирует `AudioRecord` (16kHz, Mono, 16-bit PCM)
3. Аудио буфер собирается в `ShortArray`
4. `processAudio()` конвертирует `ShortArray` → `FloatArray` (нормализация)
5. JNI вызов `WhisperJNI_recognize()` передаёт данные в C++
6. `whisper_full()` обрабатывает аудио через модель `ggml-tiny.bin`
7. Результат возвращается как строка через JNI
8. Текст отображается на экране, пользователь может сохранить как `Note`

### Сохранение заметки:

1. `NotesViewModel.addNote()` создаёт `NoteEntity`
2. `NoteDao.insertNote()` записывает в Room БД (фоновый поток, Dispatchers.IO)
3. LiveData уведомляет UI об обновлении списка заметок

## 5. Зависимости (build.gradle.kts)

| Группа | Артефакт | Назначение |
|--------|----------|------------|
| `androidx.compose` | compose-bom, ui, material3 | UI на Compose |
| `androidx.lifecycle` | lifecycle-runtime-ktx | ViewModel, LiveData |
| `androidx.activity` | activity-compose | ComponentActivity |
| `androidx.hilt` | hilt-navigation-compose | ViewModel навигация |
| `androidx.room` | room-runtime, room-ktx | Локальная БД |
| CMake + NDK | whisper.cpp, GGML | Нативное распознавание речи |

## 6. Правила кода (из .clinerules)

- **MVVM / Clean Architecture:** бизнес-логика не в `View` (Composable)
- **@MainActor:** обновления UI во ViewModel строго на главном потоке
- **Нет `!`:** только `if let` / `guard let`
- **Один файл — одна сущность**
- **Приватные модификаторы:** внутренние свойства и методы — `private`
- **MARK-секции:** файлы >100 строк разбиваются на `// MARK: - Properties`, `// MARK: - Initialization`, `// MARK: - Methods`
- **UIImpactFeedbackGenerator:** тактильный отклик на ключевые кнопки
- **SF Symbols:** только системные иконки Apple
- **Dark Mode:** системные цветовые токены
- **Адаптивная верстка:** без хардкода размеров, только `Spacer`, `Padding`, `GeometryReader`

## 7. Ограничения

- `whisper_context` — небезопасный для потоков глобальный указатель (single-use)
- Модель `ggml-tiny.bin` (~16 MB) включается в APK (assets)
- Требуются Android SDK + NDK для сборки нативного кода (CMake)
- Минимальная версия Android: API 24 (minSdk), targetSdk 34

## 8. Правило обновления ARCHITECTURE.md и README.md

**ОБЯЗАТЕЛЬНОЕ ПРАВИЛО:** При каждом коммите (`git commit`) необходимо проверить, требуется ли обновление `ARCHITECTURE.md` и/или `README.md`, если были внесены изменения в любую из следующих областей:

| Изменение | Требуется обновить |
|-----------|-------------------|
| Добавление/удаление/изменение слоёв архитектуры (UI, ViewModel, Service, Data, Native) | `ARCHITECTURE.md` |
| Изменение JNI-интерфейса (новые нативные функции, изменение сигнатур) | `ARCHITECTURE.md` |
| Изменение схемы Room БД (новые таблицы, поля, DAO) | `ARCHITECTURE.md` |
| Новые ViewModel или изменение их ответственности | `ARCHITECTURE.md` |
| Изменение зависимостей (build.gradle.kts, libs.versions.toml) | `ARCHITECTURE.md` + `README.md` |
| Изменение структуры проекта (новые папки, перемещение файлов) | `ARCHITECTURE.md` + `README.md` |
| Изменение инструкций по сборке (SDK, NDK, Gradle команды) | `README.md` |
| Изменение ключевых возможностей (Killer-Features) | `README.md` |
| Изменение технологического стека | `ARCHITECTURE.md` + `README.md` |

**Процесс перед коммитом:**

1. Проверить `git diff --stat` на наличие изменений в вышеуказанных областях
2. Если изменения затрагивают архитектуру — обновить `ARCHITECTURE.md`
3. Если изменения затрагивают стек, структуру или сборку — обновить `README.md`
4. Внести оба файла в тот же коммит, что и изменения кода
5. Обновить `.cline-logs/log-NNN.md` с описанием проделанной работы

**Формат лога коммита:** `Log XXX: [Описание задачи на русском]` (см. правила `git-changelog`)

