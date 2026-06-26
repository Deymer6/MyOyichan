package com.upn.myoyichan.ui.auth

import android.os.Bundle
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import com.upn.myoyichan.data.local.AppDatabase
import com.upn.myoyichan.databinding.ActivityRecoverBinding
import com.upn.myoyichan.repository.UsuarioRepository
import com.upn.myoyichan.ui.BaseActivity
import com.upn.myoyichan.ui.ViewModelFactory

class RecoverPasswordActivity : BaseActivity() {

    private lateinit var binding: ActivityRecoverBinding
    private lateinit var viewModel: AuthViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRecoverBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val repository = UsuarioRepository(AppDatabase.getInstance(this).usuarioDao())
        val factory = ViewModelFactory(usuarioRepository = repository)
        viewModel = ViewModelProvider(this, factory)[AuthViewModel::class.java]

        setupListeners()
        setupObservers()
    }

    private fun setupListeners() {
        binding.btnBack.setOnClickListener { finish() }

        binding.btnChangePassword.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val newPassword = binding.etNewPassword.text.toString().trim()
            val confirmPassword = binding.etConfirmPassword.text.toString().trim()

            var isValid = true

            if (email.isEmpty()) { binding.tilEmail.error = "Requerido"; isValid = false } else binding.tilEmail.error = null
            if (newPassword.isEmpty()) { binding.tilNewPassword.error = "Requerido"; isValid = false } else binding.tilNewPassword.error = null
            if (newPassword != confirmPassword) { binding.tilConfirmPassword.error = "Las contraseñas no coinciden"; isValid = false } else binding.tilConfirmPassword.error = null

            if (isValid) {
                viewModel.recoverPassword(email, newPassword)
            }
        }
    }

    private fun setupObservers() {
        viewModel.recoverResult.observe(this) { result ->
            result.onSuccess {
                Toast.makeText(this, "Contraseña actualizada exitosamente", Toast.LENGTH_SHORT).show()
                finish()
            }.onFailure { exception ->
                Toast.makeText(this, exception.message, Toast.LENGTH_SHORT).show()
            }
        }
    }
}
