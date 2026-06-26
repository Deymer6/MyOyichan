package com.upn.myoyichan.utils

import android.content.Context
import android.content.SharedPreferences

class SessionManager(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREFS_NAME = "myoyichan_session"
        private const val KEY_USER_ID = "usuario_id"
        private const val KEY_USER_NAME = "usuario_nombre"
        private const val KEY_IS_LOGGED_IN = "is_logged_in"
    }

    fun saveSession(userId: Int, nombre: String) {
        prefs.edit().apply {
            putInt(KEY_USER_ID, userId)
            putString(KEY_USER_NAME, nombre)
            putBoolean(KEY_IS_LOGGED_IN, true)
            apply()
        }
    }

    fun getUsuarioId(): Int = prefs.getInt(KEY_USER_ID, -1)

    fun getNombre(): String = prefs.getString(KEY_USER_NAME, "") ?: ""

    fun isLoggedIn(): Boolean = prefs.getBoolean(KEY_IS_LOGGED_IN, false)

    fun clearSession() {
        prefs.edit().clear().apply()
    }
}
