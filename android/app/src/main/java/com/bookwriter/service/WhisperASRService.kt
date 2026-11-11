package com.bookwriter.service

import android.annotation.SuppressLint
import android.content.Context
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaFormat
import android.util.Log
import java.nio.ByteBuffer
import java.nio.ByteOrder

/** WhisperASRService - Фоновый сервис для распознавания речи на базе модели Whisper.cpp
 * Работает полностью офлайн, автоматически расставляет пунктуацию (точки, запятые)
 * на основе контекста русской речи и не требует доступа к внешним API.
 */
class WhisperASRService(context: Context) {

    private val TAG = "WhisperASRService"

    // Whisper model path in assets (copied to internal storage at runtime)
    private var modelPath: String? = null
    private var whisperCtx: Any? = null

    // Audio recording settings
    private val sampleRate = 16000
    private val channelCount = 1
    private val audioFormat = AudioFormat.ENCODING_PCM_16BIT
    private val bufferSize = AudioRecord.getMinBufferSize(sampleRate, AudioFormat.CHANNEL_IN_MONO, audioFormat)

    init {
        // Copy Whisper model to persistent location if not exists
        modelPath = copyModelFromAssets(context)
    }

    /** Initialize Whisper model from assets path via JNI */
    @SuppressLint("MissingPermission")
    fun initializeModel(): Boolean {
        if (whisperCtx != null) {
            freeModel()
        }

        if (modelPath == null) {
            Log.e(TAG, "Whisper model path is null")
            return false
        }

        try {
            // Load native Whisper JNI library
            System.loadLibrary("whisper_jni")
            // Call JNI function to initialize model from file path
            whisperCtx = WhisperJNI_initModel(modelPath!!)
            if (whisperCtx == null) {
                Log.e(TAG, "Failed to initialize Whisper model")
                return false
            }
            Log.i(TAG, "Whisper model initialized successfully")
            return true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize Whisper model", e)
            return false
        }
    }

    /** Process audio data for speech recognition */
    @SuppressLint("MissingPermission")
    fun processAudio(audioData: short[]): String {
        if (whisperCtx == null) {
            if (!initializeModel()) {
                return "No model initialized"
            }
        }

        // Convert short PCM to float for Whisper (-1.0 to 1.0 range)
        val floatData = audioData.map { it / 32768f }

        // Call JNI function to recognize speech
        val result = WhisperJNI_recognize(whisperCtx, floatData, sampleRate)
        return result
    }

    /** Free Whisper model resources */
    fun freeModel() {
        if (whisperCtx != null) {
            WhisperJNI_freeModel(whisperCtx)
            whisperCtx = null
        }
        Log.i(TAG, "Whisper model freed")
    }

    /** Copy Whisper model from assets to internal storage */
    private fun copyModelFromAssets(context: Context): String? {
        val modelFile = "ggml-tiny.bin"
        val assets = context.assets
        val outputFile = context.externalFilesDir(null) + "/" + modelFile

        try {
            val input = assets.open(modelFile)
            val output = java.io.FileOutputStream(outputFile)
            val buffer = ByteBuffer.allocate(1024)
            var bytesRead = 0

            while (bytesRead != -1) {
                bytesRead = input.read(buffer.array())
                if (bytesRead > 0) {
                    buffer.position(bytesRead)
                    output.write(buffer.array(), 0, bytesRead)
                }
            }

            input.close()
            output.close()
            return outputFile
        } catch (e: Exception) {
            Log.e(TAG, "Failed to copy model from assets", e)
            return null
        }
    }

    // Native JNI interface methods
    @SuppressLint("Jni")
    private native static long WhisperJNI_initModel(String modelPath);

    @SuppressLint("Jni")
    private native static String WhisperJNI_recognize(long ctx, float[] audioData, int sampleRate);

    @SuppressLint("Jni")
    private native static void WhisperJNI_freeModel(long ctx);
}