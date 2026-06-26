package com.upn.myoyichan.utils

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.util.Log
import java.util.Locale

class VoiceManager(private val context: Context) {

    private var tts: TextToSpeech? = null
    private var speechRecognizer: SpeechRecognizer? = null
    private var isTtsReady = false

    // Velocidad y tono iniciales según las preferencias guardadas en Ajustes
    private val prefs = PreferencesManager(context)
    private var speechRate = prefs.voiceSpeed
    private var speechPitch = prefs.voicePitch

    companion object {
        private const val TAG = "VoiceManager"
    }

    init {
        initTts()
    }

    private fun initTts() {
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                val locale = Locale("es", "PE")
                val result = tts?.setLanguage(locale)
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    // Intentar español genérico si Perú no está disponible
                    tts?.setLanguage(Locale("es"))
                }
                tts?.setSpeechRate(speechRate)
                tts?.setPitch(speechPitch)
                isTtsReady = true
                Log.d(TAG, "TextToSpeech inicializado correctamente")
            } else {
                Log.e(TAG, "Error al inicializar TextToSpeech: $status")
            }
        }
    }

    fun hablar(texto: String) {
        if (isTtsReady) {
            tts?.speak(texto, TextToSpeech.QUEUE_FLUSH, null, "utterance_${System.currentTimeMillis()}")
        } else {
            Log.w(TAG, "TextToSpeech no está listo")
        }
    }

    fun detenerHabla() {
        tts?.stop()
    }

    fun setSpeechRate(rate: Float) {
        speechRate = rate
        tts?.setSpeechRate(rate)
    }

    fun setSpeechPitch(pitch: Float) {
        speechPitch = pitch
        tts?.setPitch(pitch)
    }

    fun escuchar(onResult: (String) -> Unit, onError: (String) -> Unit) {
        if (androidx.core.content.ContextCompat.checkSelfPermission(context, android.Manifest.permission.RECORD_AUDIO) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
            onError("Se requieren permisos de micrófono para esta función.")
            return
        }

        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            onError("Reconocimiento de voz no disponible en este dispositivo")
            return
        }

        speechRecognizer?.destroy()
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context)

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "es-PE")
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, false)
        }

        speechRecognizer?.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                Log.d(TAG, "Listo para escuchar")
            }

            override fun onBeginningOfSpeech() {
                Log.d(TAG, "Inicio de habla detectado")
            }

            override fun onRmsChanged(rmsdB: Float) {}

            override fun onBufferReceived(buffer: ByteArray?) {}

            override fun onEndOfSpeech() {
                Log.d(TAG, "Fin de habla detectado")
            }

            override fun onError(error: Int) {
                val errorMsg = when (error) {
                    SpeechRecognizer.ERROR_AUDIO -> "Error de audio"
                    SpeechRecognizer.ERROR_CLIENT -> "Error del cliente"
                    SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Permisos insuficientes"
                    SpeechRecognizer.ERROR_NETWORK -> "Error de red"
                    SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Timeout de red"
                    SpeechRecognizer.ERROR_NO_MATCH -> "No se entendió el comando"
                    SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Reconocedor ocupado"
                    SpeechRecognizer.ERROR_SERVER -> "Error del servidor"
                    SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "No se detectó habla"
                    else -> "Error desconocido"
                }
                Log.e(TAG, "Error de reconocimiento: $errorMsg")
                onError(errorMsg)
            }

            override fun onResults(results: Bundle?) {
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                val text = matches?.firstOrNull() ?: ""
                Log.d(TAG, "Resultado: $text")
                onResult(text.lowercase(Locale.getDefault()))
            }

            override fun onPartialResults(partialResults: Bundle?) {}

            override fun onEvent(eventType: Int, params: Bundle?) {}
        })

        speechRecognizer?.startListening(intent)
    }

    fun detenerEscucha() {
        speechRecognizer?.stopListening()
    }

    fun destroy() {
        tts?.stop()
        tts?.shutdown()
        tts = null
        speechRecognizer?.destroy()
        speechRecognizer = null
        isTtsReady = false
    }
}
