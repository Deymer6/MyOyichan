package com.upn.myoyichan.utils

import android.content.Context
import android.content.SharedPreferences

/**
 * Preferencias de la app configurables desde la pantalla de Ajustes
 * (velocidad/tono de voz, activación del asistente, notificaciones y tamaño de fuente).
 * Se guardan en un archivo aparte del de la sesión ([SessionManager]).
 */
class PreferencesManager(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREFS_NAME = "myoyichan_settings"
        private const val KEY_VOICE_SPEED = "voice_speed"
        private const val KEY_VOICE_PITCH = "voice_pitch"
        private const val KEY_VOICE_ASSISTANT = "voice_assistant_enabled"
        private const val KEY_NOTIFICATIONS = "notifications_enabled"
        private const val KEY_FONT_SIZE = "font_size_index"

        // Escalas correspondientes a R.array.tamano_fuente_array: Normal, Grande, Muy Grande
        private val FONT_SCALES = floatArrayOf(1.0f, 1.15f, 1.3f)
    }

    var voiceSpeed: Float
        get() = prefs.getFloat(KEY_VOICE_SPEED, 1.0f)
        set(value) = prefs.edit().putFloat(KEY_VOICE_SPEED, value).apply()

    var voicePitch: Float
        get() = prefs.getFloat(KEY_VOICE_PITCH, 1.0f)
        set(value) = prefs.edit().putFloat(KEY_VOICE_PITCH, value).apply()

    var voiceAssistantEnabled: Boolean
        get() = prefs.getBoolean(KEY_VOICE_ASSISTANT, true)
        set(value) = prefs.edit().putBoolean(KEY_VOICE_ASSISTANT, value).apply()

    var notificationsEnabled: Boolean
        get() = prefs.getBoolean(KEY_NOTIFICATIONS, true)
        set(value) = prefs.edit().putBoolean(KEY_NOTIFICATIONS, value).apply()

    /** Índice dentro de R.array.tamano_fuente_array (0 = Normal, 1 = Grande, 2 = Muy Grande). */
    var fontSizeIndex: Int
        get() = prefs.getInt(KEY_FONT_SIZE, 0)
        set(value) = prefs.edit().putInt(KEY_FONT_SIZE, value.coerceIn(0, FONT_SCALES.size - 1)).apply()

    /** Factor de escala de fuente que aplica [com.upn.myoyichan.ui.BaseActivity] a toda la app. */
    fun fontScale(): Float = FONT_SCALES[fontSizeIndex.coerceIn(0, FONT_SCALES.size - 1)]
}
