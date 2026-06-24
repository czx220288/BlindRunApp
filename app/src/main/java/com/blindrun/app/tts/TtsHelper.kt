package com.blindrun.app.tts

import android.content.Context
import android.speech.tts.TextToSpeech
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TtsHelper @Inject constructor(private val context: Context) {
    private var tts: TextToSpeech? = null
    private var isReady = false

    init {
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                try {
                    val result = tts?.setLanguage(Locale.CHINESE)
                    if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                        // 回退到英语
                        tts?.setLanguage(Locale.US)
                    }
                    isReady = true
                } catch (e: Exception) {
                    e.printStackTrace()
                    isReady = false
                }
            } else {
                isReady = false
            }
        }
    }

    fun speak(text: String) {
        if (isReady && tts != null) {
            try {
                tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun onDestroy() {
        try {
            tts?.stop()
            tts?.shutdown()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        isReady = false
        tts = null
    }
}
