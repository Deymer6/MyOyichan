package com.upn.myoyichan.utils

import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.TranslatorOptions
import kotlinx.coroutines.tasks.await

class TranslatorManager {

    private val options = TranslatorOptions.Builder()
        .setSourceLanguage(TranslateLanguage.ENGLISH)
        .setTargetLanguage(TranslateLanguage.SPANISH)
        .build()

    private val englishSpanishTranslator = Translation.getClient(options)
    
    // Descarga el modelo de 30MB solo si no existe en el celular
    suspend fun prepareModel(): Boolean {
        return try {
            // Sin requireWifi: permite descargar el modelo también con datos móviles
            val conditions = DownloadConditions.Builder().build()
            englishSpanishTranslator.downloadModelIfNeeded(conditions).await()
            true
        } catch (e: kotlinx.coroutines.CancellationException) {
            throw e // Dejar que el timeout cancele correctamente
        } catch (e: Exception) {
            false
        }
    }

    suspend fun translate(text: String): String {
        if (text.isBlank() || text == "No especificado" || text == "-") return text
        return try {
            englishSpanishTranslator.translate(text).await()
        } catch (e: kotlinx.coroutines.CancellationException) {
            throw e // Dejar que el timeout cancele correctamente
        } catch (e: Exception) {
            // Si falla la traducción (ej. no se descargó el modelo), devolvemos el original
            text
        }
    }

    fun close() {
        englishSpanishTranslator.close()
    }
}
