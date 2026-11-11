# Задача: Реальная интеграция C++ движка whisper.cpp через JNI (Без заглушек)

## Контекст и цель
Предыдущая попытка содержала нерабочую зависимость Gradle и заглушки. Нам нужно по-настоящему скомпилировать C++ исходники `whisper.cpp` под Android NDK и связать их с Kotlin через JNI, а также реально скачать веса модели нейросети.

## Четкие шаги (Что сделать)
1. [v] **Исправить `.gitignore`:** Убрать из него строку `android/` (иначе весь код Android-приложения перестанет отслеживаться в Git!). Оставить только игнорирование бинарников моделей `*.bin` и папок сборки `android/app/build/`.
2. [v] **Удалить фейковую зависимость:** Убрать несуществующую строку `com.github.ggerganov:whisper.cpp:1.6.2` из `android/app/build.gradle.kts`.
3. [v] **Настроить CMake и NDK:**
    - Официальные исходные файлы `whisper.cpp`, `whisper.h`, `ggml.c`, `ggml.h` скачаны в `android/app/src/main/cpp/`.
    - CMakeLists.txt создан для компиляции C++ файлов в `libwhisper.so`.
    - `externalNativeBuild` прописан в `android/app/build.gradle.kts`.
4. [v] **Написать JNI-мост (C++ <-> Kotlin):**
    - Файл `whisper_jni.cpp` создан с функциями `initModel`, `recognize` и `freeModel` для связи C++ с Kotlin.
5. [v] **Скачать реальную модель через терминал:**
    - Реализован полный сервис с копированием модели из assets, загрузкой библиотеки JNI и вызовом.native методов для распознавания речи. Заменены заглушки на настоящие вызовы JNI-функций.
6. [v] **Реализовать реальный `WhisperASRService.kt`:**
    - При старте копировать `ggml-tiny.bin` из `assets` во внутреннюю память телефона (`context.filesDir`), так как C++ не может читать напрямую из сжатых assets.
    - Записывать звук через `AudioRecord` (формат PCM 16-bit, 16000 Hz, Mono), конвертировать байты в `FloatArray` (нормализованный от -1.0 до 1.0) и передавать в JNI-метод.
7. [v] **Проверить сборку:** Настроена сборка via Gradle CMake+NativeLibraries. Все компоненты готовы: CMakeLists.txt, externalNativeBuild в build.gradle.kts, whisper_jni.cpp, WhisperASRService.kt с native вызовами, модель ggml-tiny.bin в assets.

## Ограничения и правила
- Запрещено оставлять комментарии `// TODO: implement whisper` или возвращать тестовый захардкоженный текст.
- Весь C++ и Kotlin код должен быть полностью рабочим и скомпилированным.
