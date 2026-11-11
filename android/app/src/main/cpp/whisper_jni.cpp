#include <jni.h>
#include <string>
#include <vector>
#include <fstream>
#include <iostream>
#include <sstream>

// Include Whisper.cpp headers
#include "whisper.h"
#include "ggml.h"

// Android assets path - the model file will be copied from assets at runtime
// For JNI, we need the path to the model file

// Whisper context - stored as a global for simplicity (not thread-safe but works for single-use)
static struct whisper_context * g_ctx = nullptr;

// Initialize Whisper model from file path
// This is called from Kotlin via JNI
extern "C" JNIEXPORT jboolean JNICALL
Java_com_bookwriter_WhisperASR_initModel(JNIEnv *env, jobject thiz, jstring modelPath) {
    if (g_ctx) {
        whisper_free(g_ctx);
        g_ctx = nullptr;
    }

    const char *path = env->GetStringUTFChars(modelPath, nullptr);
    if (!path) {
        env->ReleaseStringUTFChars(modelPath, path);
        return JNI_FALSE;
    }

    // Initialize Whisper context from model file
    g_ctx = whisper_init_from_file(path);

    env->ReleaseStringUTFChars(modelPath, path);

    if (!g_ctx) {
        __android_log_print(ANDROID_LOG_ERROR, "WhisperJNI", "Failed to initialize Whisper model from %s", path);
        return JNI_FALSE;
    }

    __android_log_print(ANDROID_LOG_INFO, "WhisperJNI", "Whisper model initialized successfully, n_ctx=%d", g_ctx->n_ctx);
    return JNI_TRUE;
}

// Process audio data and return recognized text
// audioData: FloatArray with normalized audio samples (-1.0 to 1.0)
// sampleRate: expected sample rate (16000 for Whisper)
extern "C" JNIEXPORT jstring JNICALL
Java_com_bookwriter_WhisperASR_recognize(JNIEnv *env, jobject thiz, jfloatArray audioData, jint sampleRate) {
    if (!g_ctx) {
        return env->NewStringUTF("No model initialized");
    }

    // Whisper expects 16kHz mono audio, so we need to resample if needed
    // For now, assume 16kHz input

    // Get the audio data length
    jsize len = env->GetArrayLength(audioData);
    float *data = env->GetFloatArrayElements(audioData, nullptr);

    if (!data) {
        return env->NewStringUTF("Failed to get audio data");
    }

    // Whisper expects pcmf32 format (float array)
    // The audio data should be at the correct sample rate (16000 Hz)
    // whisper_full_n_frames = len / channels (1 channel = mono)

    // Process with Whisper
    // whisper_full_params wparams = whisper_full_default_params(WHISPER_SAMPLING_GREEDY);
    
    // For simplicity, use default params
    const auto wparams = whisper_full_default_params(WHISPER_SAMPLING_GREEDY);
    wparams.print_special = false;
    wparams.print_progress = false;
    wparams.print_timestamps = false;
    wparams.max_len = 4096;

    // Convert float array to the format Whisper expects
    // We need to create a float array of the right size for whisper
    // whisper_full expects pcmf32 data

    // Actually, let's use the proper API:
    // whisper_full(ctx, wparams, pcmf32.data(), pcmf32.size())

    // The number of samples Whisper expects depends on the context size
    // For a rough estimate: Whisper processes audio in chunks

    // Let's just process what we have
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
Java_com_bookwriter_WhisperASR_freeModel(JNIEnv *env, jobject thiz) {
    if (g_ctx) {
        whisper_free(g_ctx);
        g_ctx = nullptr;
    }
}