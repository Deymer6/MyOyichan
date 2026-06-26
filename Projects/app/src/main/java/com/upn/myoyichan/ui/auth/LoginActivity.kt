package com.upn.myoyichan.ui.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import com.upn.myoyichan.data.local.AppDatabase
import com.upn.myoyichan.databinding.ActivityLoginBinding
import com.upn.myoyichan.repository.UsuarioRepository
import com.upn.myoyichan.ui.BaseActivity
import com.upn.myoyichan.ui.ViewModelFactory
import com.upn.myoyichan.ui.main.MainActivity
import com.upn.myoyichan.utils.SessionManager

class LoginActivity : BaseActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var viewModel: AuthViewModel
    private lateinit var sessionManager: SessionManager
    private lateinit var voiceManager: com.upn.myoyichan.utils.VoiceManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)
        voiceManager = com.upn.myoyichan.utils.VoiceManager(this)
        
        // Redirigir a MainActivity si ya está logueado
        if (sessionManager.isLoggedIn()) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            return
        }

        val repository = UsuarioRepository(AppDatabase.getInstance(this).usuarioDao())
        val factory = ViewModelFactory(usuarioRepository = repository)
        viewModel = ViewModelProvider(this, factory)[AuthViewModel::class.java]

        setupListeners()
        setupObservers()
    }

    private fun setupListeners() {
        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            var isValid = true

            if (email.isEmpty()) {
                val errorMsg = "Ingrese su correo"
                binding.tilEmail.error = errorMsg
                voiceManager.hablar(errorMsg)
                isValid = false
            } else {
                binding.tilEmail.error = null
            }

            if (password.isEmpty()) {
                val errorMsg = "Ingrese su contraseña"
                binding.tilPassword.error = errorMsg
                voiceManager.hablar(errorMsg)
                isValid = false
            } else {
                binding.tilPassword.error = null
            }

            if (isValid) {
                viewModel.login(email, password)
            }
        }

        binding.btnRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        binding.btnForgotPassword.setOnClickListener {
            startActivity(Intent(this, RecoverPasswordActivity::class.java))
        }
    }

    private fun setupObservers() {
        viewModel.loginResult.observe(this) { result ->
            result.onSuccess { usuario ->
                mostrarDialogo2FA(usuario)
            }.onFailure { exception ->
                Toast.makeText(this, exception.message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun mostrarDialogo2FA(usuario: com.upn.myoyichan.data.local.entity.UsuarioEntity) {
        val tokenGenerado = kotlin.random.Random.nextInt(100000, 999999).toString()
        // En una app real esto se envía por SMS/Email. Para el prototipo, lo mostramos en un Toast
        Toast.makeText(this, "Tu código 2FA es: $tokenGenerado", Toast.LENGTH_LONG).show()

        val input = android.widget.EditText(this)
        input.inputType = android.text.InputType.TYPE_CLASS_NUMBER
        input.hint = "Ingresa el código"
        input.setPadding(50, 20, 50, 20)

        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Verificación 2FA")
            .setMessage("Por favor ingresa el código de 6 dígitos para continuar.")
            .setView(input)
            .setPositiveButton("Verificar") { _, _ ->
                val code = input.text.toString().trim()
                if (code == tokenGenerado) {
                    sessionManager.saveSession(usuario.id, usuario.nombre)
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                } else {
                    Toast.makeText(this, "Código incorrecto", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancelar") { dialog, _ ->
                dialog.cancel()
            }
            .setCancelable(false)
            .show()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::voiceManager.isInitialized) {
            voiceManager.destroy()
        }
    }
}
