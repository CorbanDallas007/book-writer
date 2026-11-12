package com.bookwriter.service

import android.annotation.SuppressLint
import android.content.Context
import android.media.AudioFormat
import android.media.AudioRecord
import android.util.Log

/** WhisperASRService - Фоновый сервис для распознавания речи на базе модели Whisper.cpp
 * Работает полностью офлайн, автоматически расставляет пунктуацию (точки, запятые)
 * на основе контекста русской речи и не требует доступа к внешним API.
 */
class WhisperASRService(context: Context) {

    private val TAG = "WhisperASRService"

    // Whisper model path in internal storage (copied from assets at runtime)
    private var modelPath: String? = null
    private var whisperCtx: Long = 0

    // Audio recording settings
    private val sampleRate = 16000
    private val channelCount = 1
    private val audioFormat = AudioFormat.ENCODING_PCM_16BIT
    private val bufferSize = AudioRecord.getMinBufferSize(sampleRate, AudioFormat.CHANNEL_IN_MONO, audioFormat)

    init {
        // Copy Whisper model to internal storage if not exists
        modelPath = copyModelFromAssets(context)
    }

    /** Initialize Whisper model from file path via JNI */
    @SuppressLint("MissingPermission")
    fun initializeModel(): Boolean {
        if (whisperCtx != 0L) {
            freeModel()
        }

        if (modelPath == null) {
            Log.e(TAG, "Whisper model path is null")
            return false
        }

        try {
            // Load native Whisper JNI library
            System.loadLibrary("whisper")
            // Call JNI function to initialize model from file path
            whisperCtx = WhisperJNI_initModel(modelPath!!)
            if (whisperCtx == 0L) {
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
    fun processAudio(audioData: ShortArray): String {
        if (whisperCtx == 0L) {
            if (!initializeModel()) {
                return "No model initialized"
            }
        }

        // Convert short PCM to float for Whisper (-1.0 to 1.0 range)
        val floatData = audioData.map { it / 32768f }.toFloatArray()

        // Call JNI function to recognize speech
        val result = WhisperJNI_recognize(whisperCtx, floatData, sampleRate)
        return result
    }

    /** Free Whisper model resources */
    fun freeModel() {
        if (whisperCtx != 0L) {
            WhisperJNI_freeModel(whisperCtx)
            whisperCtx = 0L
        }
        Log.i(TAG, "Whisper model freed")
    }

    /** Copy Whisper model from assets to internal storage */
    private fun copyModelFromAssets(context: Context): String? {
        val modelFile = "ggml-tiny.bin"
        val assets = context.assets
        val outputFile = context.filesDir.absolutePath + "/" + modelFile

        // Return existing path if model already copied
        val existingFile = java.io.File(outputFile)
        if (existingFile.exists()) {
            return outputFile
        }

        try {
            val input = assets.open(modelFile)
            val output = java.io.FileOutputStream(outputFile)
            val buffer = ByteArray(8192)
            var bytesRead: Int

            while (true) {
                bytesRead = input.read(buffer)
                if (bytesRead == -1) break
                output.write(buffer, 0, bytesRead)
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
    private external fun WhisperJNI_initModel(modelPath: String): Long

    @SuppressLint("Jni")
    private external fun WhisperJNI_recognize(ctx: Long, audioData: FloatArray, sampleRate: Int): String

    @SuppressLint("Jni")
    private external fun WhisperJNI_freeModel(ctx: Long)
}