package com.upn.myoyichan.ui

import android.content.Context
import android.content.res.Configuration
import androidx.appcompat.app.AppCompatActivity
import com.upn.myoyichan.utils.PreferencesManager

/**
 * Activity base que aplica el tamaño de fuente elegido en Ajustes a todas las pantallas.
 * Todas las Activities heredan de aquí para que la preferencia sea global; los Fragments
 * usan el contexto de su Activity anfitriona, por lo que también se reescalan.
 */
abstract class BaseActivity : AppCompatActivity() {

    override fun attachBaseContext(newBase: Context) {
        val config = Configuration(newBase.resources.configuration)
        config.fontScale = PreferencesManager(newBase).fontScale()
        super.attachBaseContext(newBase.createConfigurationContext(config))
    }
}
