package com.upn.myoyichan.ui.main

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.upn.myoyichan.R
import com.upn.myoyichan.data.local.AppDatabase
import com.upn.myoyichan.databinding.ActivityMainBinding
import com.upn.myoyichan.repository.ContactoSosRepository
import com.upn.myoyichan.ui.auth.LoginActivity
import com.upn.myoyichan.ui.medicamentos.InfoMedicamentoActivity
import com.upn.myoyichan.ui.medicamentos.MedicamentoFormActivity
import com.upn.myoyichan.ui.profile.ProfileActivity
import com.upn.myoyichan.ui.BaseActivity
import com.upn.myoyichan.ui.settings.SettingsActivity
import com.upn.myoyichan.ui.vitales.VitalFormActivity
import com.upn.myoyichan.utils.PreferencesManager
import com.upn.myoyichan.utils.SessionManager
import com.upn.myoyichan.utils.VoiceManager
import kotlinx.coroutines.launch

class MainActivity : BaseActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var sessionManager: SessionManager
    private lateinit var voiceManager: VoiceManager
    private lateinit var preferencesManager: PreferencesManager
    private lateinit var contactoSosRepository: ContactoSosRepository

    private var isListening = false

    companion object {
        private const val REQUEST_RECORD_AUDIO_PERMISSION = 200
        private const val REQUEST_CALL_PHONE_PERMISSION = 201
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)
        if (!sessionManager.isLoggedIn()) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        contactoSosRepository = ContactoSosRepository(AppDatabase.getInstance(this).contactoSosDao())
        voiceManager = VoiceManager(this)
        preferencesManager = PreferencesManager(this)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(systemBars.left, systemBars.top, systemBars.right, 0)
            binding.bottomNav.setPadding(0, 0, 0, systemBars.bottom)
            insets
        }

        setupNavigation()
        setupToolbar()
        setupVoiceAssistant()
    }

    private fun setupNavigation() {
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.navHostFragment) as NavHostFragment
        val navController = navHostFragment.navController
        binding.bottomNav.setupWithNavController(navController)
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        binding.toolbar.title = "Hola, ${sessionManager.getNombre()}"

        binding.btnProfile.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }

        binding.btnSettings.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }
    }

    private fun setupVoiceAssistant() {
        binding.fabVoice.setOnClickListener {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO), REQUEST_RECORD_AUDIO_PERMISSION)
                return@setOnClickListener
            }

            if (isListening) {
                voiceManager.detenerEscucha()
                binding.fabVoice.setImageResource(R.drawable.ic_mic)
                isListening = false
            } else {
                Toast.makeText(this, "Escuchando...", Toast.LENGTH_SHORT).show()
                binding.fabVoice.setImageResource(R.drawable.ic_mic) // Change icon to indicate listening if you have one
                isListening = true
                voiceManager.escuchar(
                    onResult = { text ->
                        isListening = false
                        binding.fabVoice.setImageResource(R.drawable.ic_mic)
                        handleVoiceCommand(text)
                    },
                    onError = { error ->
                        isListening = false
                        binding.fabVoice.setImageResource(R.drawable.ic_mic)
                        Toast.makeText(this, error, Toast.LENGTH_SHORT).show()
                    }
                )
            }
        }
    }

    private fun handleVoiceCommand(rawCommand: String) {
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.navHostFragment) as NavHostFragment
        val navController = navHostFragment.navController

        // Normalizar texto: quitar tildes y pasar a minúsculas
        val command = java.text.Normalizer.normalize(rawCommand.lowercase(), java.text.Normalizer.Form.NFD)
            .replace("\\p{InCombiningDiacriticalMarks}+".toRegex(), "")

        when {
            // Lógica para crear/agregar medicamentos o recordatorios
            command.matches(".*(crear|agregar|anadir|nuevo|nueva|registrar|toca|anotar|recordar).*(medicamento|medicina|pastilla|recordatorio|remedio).*".toRegex()) -> {
                startActivity(Intent(this, MedicamentoFormActivity::class.java))
                voiceManager.hablar("Abriendo formulario para nuevo medicamento o recordatorio")
            }
            // Lógica para listar medicamentos
            command.matches(".*(dime|cuales|mostrar|ver|lista|mis).*(medicamento|medicina|pastilla).*".toRegex()) -> {
                navController.navigate(R.id.medicamentosFragment)
                voiceManager.hablar("Mostrando tus medicamentos")
            }
            // Buscar un medicamento específico
            command.matches(".*(buscar|informacion de|que es).*(medicamento|medicina|pastilla|el|la)?\\s+(.*)".toRegex()) && command.contains("buscar") -> {
                val medicamento = command.substringAfter("buscar").replace("medicamento", "").trim()
                if (medicamento.isNotEmpty()) {
                    val intent = Intent(this, InfoMedicamentoActivity::class.java)
                    intent.putExtra("nombre_medicamento", medicamento)
                    startActivity(intent)
                    voiceManager.hablar("Buscando información sobre $medicamento")
                } else {
                    voiceManager.hablar("Por favor, diga el nombre del medicamento a buscar.")
                }
            }
            // Abrir secciones de la app
            command.matches(".*(abrir|ir a|mostrar|ver).*(vitales|signos|salud).*".toRegex()) -> {
                navController.navigate(R.id.vitalesFragment)
                voiceManager.hablar("Abriendo signos vitales")
            }
            command.matches(".*(abrir|ir a|mostrar|ver).*(contacto|directorio).*".toRegex()) -> {
                navController.navigate(R.id.contactosFragment)
                voiceManager.hablar("Abriendo contactos")
            }
            // Emergencias
            command.matches(".*(emergencia|llamar|llama|socorro|ayuda|peligro|auxilio|siento mal|duele|desmayar|hijo|hija).*".toRegex()) -> {
                voiceManager.hablar("Llamando a tu contacto de emergencia")
                llamarEmergencia()
            }
            // Registrar signos vitales
            command.matches(".*(registrar|nuevo|crear|agregar|tomar|tomame|mide|medir|cuanto).*(presion|glucosa|peso|temperatura|oxigeno|azucar|fiebre).*".toRegex()) -> {
                val tipo = when {
                    command.contains("presion") -> "PRESION"
                    command.contains("glucosa") || command.contains("azucar") -> "GLUCOSA"
                    command.contains("peso") -> "PESO"
                    command.contains("temperatura") || command.contains("fiebre") -> "TEMPERATURA"
                    command.contains("oxigeno") -> "OXIGENO"
                    else -> "PRESION"
                }
                val intent = Intent(this, VitalFormActivity::class.java)
                intent.putExtra("tipo_vital", tipo)
                startActivity(intent)
                voiceManager.hablar("Abriendo registro de $tipo")
            }
            else -> {
                voiceManager.hablar(getString(R.string.voz_no_entendido))
            }
        }
    }

    fun llamarEmergencia() {
        lifecycleScope.launch {
            val userId = sessionManager.getUsuarioId()
            val contacto = contactoSosRepository.getPrincipal(userId)
            if (contacto != null) {
                realizarLlamada(contacto.telefono)
            } else {
                Toast.makeText(this@MainActivity, "No hay contacto principal configurado", Toast.LENGTH_LONG).show()
                voiceManager.hablar("No tiene un contacto principal configurado")
            }
        }
    }

    private fun realizarLlamada(telefono: String) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CALL_PHONE), REQUEST_CALL_PHONE_PERMISSION)
            return
        }
        val intent = Intent(Intent.ACTION_CALL)
        intent.data = Uri.parse("tel:$telefono")
        startActivity(intent)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_RECORD_AUDIO_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permiso concedido, puedes iniciar la escucha automáticamente o esperar al usuario
                Toast.makeText(this, "Permiso de micrófono concedido", Toast.LENGTH_SHORT).show()
            } else {
                AlertDialog.Builder(this)
                    .setTitle(R.string.voz_permiso_titulo)
                    .setMessage(R.string.voz_permiso_mensaje)
                    .setPositiveButton(R.string.voz_permiso_aceptar) { dialog, _ -> dialog.dismiss() }
                    .show()
            }
        } else if (requestCode == REQUEST_CALL_PHONE_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                llamarEmergencia()
            } else {
                Toast.makeText(this, "Permiso de llamada denegado", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Reflejar cambios hechos en Ajustes al volver a esta pantalla
        binding.fabVoice.visibility =
            if (preferencesManager.voiceAssistantEnabled) View.VISIBLE else View.GONE
        voiceManager.setSpeechRate(preferencesManager.voiceSpeed)
        voiceManager.setSpeechPitch(preferencesManager.voicePitch)
    }

    override fun onDestroy() {
        super.onDestroy()
        voiceManager.destroy()
    }
}
