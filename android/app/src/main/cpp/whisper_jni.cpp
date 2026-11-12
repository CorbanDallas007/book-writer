#include <jni.h>
#include <string>
#include <vector>
#include <android/log.h>

// Include Whisper.cpp headers
#include "whisper.h"
#include "ggml.h"

// Whisper context - stored as a global for simplicity (not thread-safe but works for single-use)
static struct whisper_context * g_ctx = nullptr;

// Initialize Whisper model from file path
// This is called from Kotlin via JNI
extern "C" JNIEXPORT jlong JNICALL
Java_com_bookwriter_WhisperASRService_initModel(JNIEnv *env, jobject thiz, jstring modelPath) {
    if (g_ctx) {
        whisper_free(g_ctx);
        g_ctx = nullptr;
    }

    const char *path = env->GetStringUTFChars(modelPath, nullptr);
    if (!path) {
        return 0;
    }

    // Initialize Whisper context from model file
    g_ctx = whisper_init_from_file(path);

    env->ReleaseStringUTFChars(modelPath, path);

    if (!g_ctx) {
        __android_log_print(ANDROID_LOG_ERROR, "WhisperJNI", "Failed to initialize Whisper model from %s", path);
        return 0;
    }

    __android_log_print(ANDROID_LOG_INFO, "WhisperJNI", "Whisper model initialized successfully");
    return (jlong)g_ctx;
}

// Process audio data and return recognized text
// audioData: FloatArray with normalized audio samples (-1.0 to 1.0)
// sampleRate: expected sample rate (16000 for Whisper)
extern "C" JNIEXPORT jstring JNICALL
Java_com_bookwriter_WhisperASRService_recognize(JNIEnv *env, jobject thiz, jlong ctx, jfloatArray audioData, jint sampleRate) {
    struct whisper_context * g_ctx = (struct whisper_context *)ctx;

    if (!g_ctx) {
        return env->NewStringUTF("No model initialized");
    }

    // Get the audio data length
    jsize len = env->GetArrayLength(audioData);
    float *data = env->GetFloatArrayElements(audioData, nullptr);

    if (!data) {
        return env->NewStringUTF("Failed to get audio data");
    }

    // Process with Whisper
    const auto wparams = whisper_full_default_params(WHISPER_SAMPLING_GREEDY);
    wparams.print_special = false;
    wparams.print_progress = false;
    wparams.print_timestamps = false;
    wparams.max_len = 4096;

    int n_samples = len;

    // Whisper full processing
    int n_tokens = whisper_full(g_ctx, wparams, data, n_samples);

    // Get the recognized text
    if (n_tokens < 0) {
        env->ReleaseFloatArrayElements(audioData, data, JNI_ABORT);
        return env->NewStringUTF("Whisper processing error");
    }

    // Get the text
    const char *text = whisper_full_get_segment_text(g_ctx, 0);

    env->ReleaseFloatArrayElements(audioData, data, JNI_ABORT);

    if (text) {
        return env->NewStringUTF(text);
    }

    return env->NewStringUTF("");
}

// Free the Whisper model
extern "C" JNIEXPORT void JNICALL
Java_com_bookwriter_WhisperASRService_freeModel(JNIEnv *env, jobject thiz, jlong ctx) {
    struct whisper_context * g_ctx = (struct whisper_context *)ctx;
    if (g_ctx) {
        whisper_free(g_ctx);
    }
}