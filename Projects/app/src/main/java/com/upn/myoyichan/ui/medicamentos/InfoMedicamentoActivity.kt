package com.upn.myoyichan.ui.medicamentos

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.upn.myoyichan.R
import com.upn.myoyichan.data.remote.RetrofitClient
import com.upn.myoyichan.databinding.ActivityInfoMedicamentoBinding
import com.upn.myoyichan.ui.BaseActivity
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull

class InfoMedicamentoActivity : BaseActivity() {

    private lateinit var binding: ActivityInfoMedicamentoBinding
    private lateinit var voiceManager: com.upn.myoyichan.utils.VoiceManager
    private lateinit var translator: com.upn.myoyichan.utils.TranslatorManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityInfoMedicamentoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setContentView(binding.root)

        voiceManager = com.upn.myoyichan.utils.VoiceManager(this)
        translator = com.upn.myoyichan.utils.TranslatorManager()

        val nombreMedicamento = intent.getStringExtra("nombre_medicamento") ?: ""
        
        binding.btnBack.setOnClickListener { finish() }

        if (nombreMedicamento.isNotEmpty()) {
            buscarEnOpenFDA(nombreMedicamento)
        } else {
            Toast.makeText(this, "Nombre de medicamento no válido", Toast.LENGTH_SHORT).show()
            finish()
        }

        binding.btnReadAloud.setOnClickListener {
            if (::voiceManager.isInitialized) {
                val textoALeer = "Resumen del medicamento. Para qué sirve: ${binding.tvPurpose.text}. " +
                        "Dosis recomendada: ${binding.tvDosage.text}. " +
                        "Advertencias principales: ${binding.tvWarnings.text}"
                // Limitamos la lectura para no saturar al usuario
                voiceManager.hablar(textoALeer.take(1000))
            }
        }
    }

    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = getSystemService(android.content.Context.CONNECTIVITY_SERVICE) as android.net.ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val activeNetwork = connectivityManager.getNetworkCapabilities(network) ?: return false
        return when {
            activeNetwork.hasTransport(android.net.NetworkCapabilities.TRANSPORT_WIFI) -> true
            activeNetwork.hasTransport(android.net.NetworkCapabilities.TRANSPORT_CELLULAR) -> true
            activeNetwork.hasTransport(android.net.NetworkCapabilities.TRANSPORT_ETHERNET) -> true
            else -> false
        }
    }

    private fun buscarEnOpenFDA(nombre: String) {
        if (!isNetworkAvailable()) {
            Toast.makeText(this, "No hay conexión a internet", Toast.LENGTH_LONG).show()
            mostrarError()
            return
        }

        binding.progressBar.visibility = View.VISIBLE
        binding.tvLoading.visibility = View.VISIBLE
        binding.tvLoading.text = "Buscando información..."
        binding.layoutContent.visibility = View.GONE

        lifecycleScope.launch {
            try {
                var result: com.upn.myoyichan.data.remote.model.DrugLabel? = null
                val queries = listOf(
                    "openfda.brand_name:\"$nombre\"",
                    "openfda.generic_name:\"$nombre\"",
                    "active_ingredient:\"$nombre\""
                )

                for (query in queries) {
                    try {
                        val response = RetrofitClient.apiService.buscarMedicamento(search = query)
                        if (response.results != null && response.results.isNotEmpty()) {
                            result = response.results[0]
                            break
                        }
                    } catch (e: Exception) {
                        // Ignoramos error (ej. 404) y probamos el siguiente campo
                    }
                }
                
                if (result != null) {
                    binding.tvLoading.text = "Traduciendo al español..."

                    // Texto original en inglés (siempre disponible aunque falle la traducción)
                    val purposeEng = (result.purpose?.joinToString("\n") ?: "No especificado").take(800)
                    val dosageEng = (result.dosageAndAdministration?.joinToString("\n") ?: "No especificado").take(800)
                    val warningsEng = (result.warnings?.joinToString("\n") ?: "No especificado").take(800)
                    val adverseEng = (result.adverseReactions?.joinToString("\n") ?: "No especificado").take(800)

                    // Intentamos traducir con ML Kit, pero con un límite de tiempo (10 s).
                    // Si el dispositivo no tiene servicios de Google o la traducción tarda
                    // demasiado, mostramos el texto en inglés en vez de quedarnos colgados.
                    val textos = withTimeoutOrNull(10_000L) {
                        translator.prepareModel()
                        val p = async { translator.translate(purposeEng) }
                        val d = async { translator.translate(dosageEng) }
                        val w = async { translator.translate(warningsEng) }
                        val a = async { translator.translate(adverseEng) }
                        listOf(p.await(), d.await(), w.await(), a.await())
                    } ?: listOf(purposeEng, dosageEng, warningsEng, adverseEng)

                    binding.progressBar.visibility = View.GONE
                    binding.tvLoading.visibility = View.GONE
                    binding.layoutContent.visibility = View.VISIBLE

                    binding.tvBrandName.text = result.openfda?.brandName?.joinToString(", ") ?: "No especificado"
                    binding.tvGenericName.text = result.openfda?.genericName?.joinToString(", ") ?: "No especificado"
                    binding.tvPurpose.text = limpiarYFormatearTexto(textos[0])
                    binding.tvDosage.text = limpiarYFormatearTexto(textos[1])
                    binding.tvWarnings.text = limpiarYFormatearTexto(textos[2])
                    binding.tvAdverseReactions.text = limpiarYFormatearTexto(textos[3])
                } else {
                    binding.progressBar.visibility = View.GONE
                    binding.tvLoading.visibility = View.GONE
                    mostrarError()
                }
            } catch (e: Exception) {
                binding.progressBar.visibility = View.GONE
                binding.tvLoading.visibility = View.GONE
                binding.layoutContent.visibility = View.VISIBLE
                mostrarError()
            }
        }
    }

    private fun mostrarError() {
        binding.tvPurpose.text = "No se encontró información en la base de datos de la FDA para este medicamento."
        binding.tvDosage.text = "-"
        binding.tvWarnings.text = "-"
        binding.tvAdverseReactions.text = "-"
        
        if (::voiceManager.isInitialized) {
            voiceManager.hablar("No he podido encontrar información médica oficial para este medicamento.")
        }
    }

    private fun limpiarYFormatearTexto(texto: String): String {
        var t = texto.replace(Regex("(?i)^(warnings?|advertencias?|liver warning|liver advertencia):\\s*"), "")
        t = t.replace(Regex("(?i)(liver warning|liver advertencia):\\s*"), "⚠️ Daño Hepático:\n")
        // Reemplaza puntos medios por saltos de línea con viñeta para listar
        t = t.replace(Regex("\\s*[•·]\\s*"), "\n• ")
        return t.trim()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::voiceManager.isInitialized) {
            voiceManager.destroy()
        }
        if (::translator.isInitialized) {
            translator.close()
        }
    }
}
