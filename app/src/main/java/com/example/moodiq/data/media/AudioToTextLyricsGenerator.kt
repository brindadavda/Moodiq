package com.example.moodiq.data.media

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer

class AudioToTextLyricsGenerator(context: Context) {
    private val appContext = context.applicationContext
    private var recognizer: SpeechRecognizer? = null
    private var isRunning = false
    private var lastTranscript = ""
    private var onTranscript: ((String) -> Unit)? = null

    fun isAvailable(): Boolean = SpeechRecognizer.isRecognitionAvailable(appContext)

    fun start(onTranscript: (String) -> Unit) {
        if (isRunning || !isAvailable()) return
        this.onTranscript = onTranscript
        isRunning = true
        lastTranscript = ""

        recognizer = SpeechRecognizer.createSpeechRecognizer(appContext).apply {
            setRecognitionListener(object : RecognitionListener {
                override fun onReadyForSpeech(params: Bundle?) = Unit
                override fun onBeginningOfSpeech() = Unit
                override fun onRmsChanged(rmsdB: Float) = Unit
                override fun onBufferReceived(buffer: ByteArray?) = Unit
                override fun onEndOfSpeech() = Unit

                override fun onError(error: Int) {
                    if (!isRunning) return
                    restart()
                }

                override fun onResults(results: Bundle?) {
                    publish(results)
                    if (isRunning) restart()
                }

                override fun onPartialResults(partialResults: Bundle?) {
                    publish(partialResults)
                }

                override fun onEvent(eventType: Int, params: Bundle?) = Unit
            })
        }

        startListening()
    }

    private fun publish(bundle: Bundle?) {
        val text = bundle
            ?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            ?.firstOrNull()
            ?.trim()
            ?.takeIf { it.isNotEmpty() }
            ?: return
        if (text.equals(lastTranscript, ignoreCase = true)) return
        lastTranscript = text
        onTranscript?.invoke(text)
    }

    private fun restart() {
        recognizer?.cancel()
        startListening()
    }

    private fun startListening() {
        recognizer?.startListening(
            Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, true)
            }
        )
    }

    fun stop() {
        isRunning = false
        onTranscript = null
        recognizer?.stopListening()
        recognizer?.cancel()
        recognizer?.destroy()
        recognizer = null
    }
}
