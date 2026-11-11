package com.bookwriter.service

import android.content.Context
import android.media.AudioRecord
import android.media.MediaFormat
import android.util.Log
import java.nio.ByteBuffer
import java.nio.ByteOrder

/**
 * WhisperASRService - Фоновый сервис для распознавания речи на базе модели Whisper.cpp
 * Работает полностью офлайн, автоматически расставляет пунктуацию (точки, запятые)
 * на основе контекста русской речи и не требует доступа к внешним API.
 */

class WhisperASRService(context: Context) {

    private val TAG = " WhisperASRService\n
