package com.upn.myoyichan.ui.profile

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.ViewModelProvider
import com.upn.myoyichan.data.local.AppDatabase
import com.upn.myoyichan.databinding.ActivityProfileBinding
import com.upn.myoyichan.repository.UsuarioRepository
import com.upn.myoyichan.ui.BaseActivity
import com.upn.myoyichan.ui.ViewModelFactory
import com.upn.myoyichan.ui.auth.AuthViewModel
import com.upn.myoyichan.ui.auth.LoginActivity
import com.upn.myoyichan.utils.SessionManager

class ProfileActivity : BaseActivity() {

    private lateinit var binding: ActivityProfileBinding
    private lateinit var viewModel: AuthViewModel
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)

        val repository = UsuarioRepository(AppDatabase.getInstance(this).usuarioDao())
        val factory = ViewModelFactory(usuarioRepository = repository)
        viewModel = ViewModelProvider(this, factory)[AuthViewModel::class.java]

        configurarSpinnerEnfermedad()
        setupListeners()
        setupObservers()

        viewModel.getUsuarioById(sessionManager.getUsuarioId())
    }

    private fun configurarSpinnerEnfermedad() {
        val opciones = resources.getStringArray(com.upn.myoyichan.R.array.enfermedades_array)
        val adapter = object : android.widget.ArrayAdapter<String>(
            this, android.R.layout.simple_spinner_item, opciones
        ) {
            override fun getView(position: Int, convertView: android.view.View?, parent: android.view.ViewGroup): android.view.View {
                val v = super.getView(position, convertView, parent) as android.widget.TextView
                v.setTextColor(context.getColor(com.upn.myoyichan.R.color.text_primary))
                v.textSize = 18f
                return v
            }

            override fun getDropDownView(position: Int, convertView: android.view.View?, parent: android.view.ViewGroup): android.view.View {
                val v = super.getDropDownView(position, convertView, parent) as android.widget.TextView
                v.setTextColor(context.getColor(com.upn.myoyichan.R.color.text_primary))
                v.textSize = 18f
                return v
            }
        }
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spEnfermedad.adapter = adapter
    }

    private fun setupListeners() {
        binding.btnBack.setOnClickListener { finish() }

        binding.btnSave.setOnClickListener {
            val usuarioActual = viewModel.usuario.value
            if (usuarioActual != null) {
                val nombre = binding.etNombre.text.toString().trim()
                val apellido = binding.etApellido.text.toString().trim()
                val edadStr = binding.etEdad.text.toString().trim()
                val telefono = binding.etTelefono.text.toString().trim()
                val enfermedad = binding.spEnfermedad.selectedItem.toString()

                var isValid = true

                if (nombre.isEmpty()) { binding.tilNombre.error = "Requerido"; isValid = false } else binding.tilNombre.error = null
                if (apellido.isEmpty()) { binding.tilApellido.error = "Requerido"; isValid = false } else binding.tilApellido.error = null
                if (edadStr.isEmpty()) { binding.tilEdad.error = "Requerido"; isValid = false } else binding.tilEdad.error = null
                if (telefono.isEmpty()) { binding.tilTelefono.error = "Requerido"; isValid = false } else binding.tilTelefono.error = null

                if (isValid) {
                    val edad = edadStr.toIntOrNull() ?: 0
                    val updatedUsuario = usuarioActual.copy(
                        nombre = nombre,
                        apellido = apellido,
                        edad = edad,
                        telefono = telefono,
                        enfermedad = enfermedad
                    )
                    viewModel.updateProfile(updatedUsuario)
                    Toast.makeText(this, "Perfil actualizado correctamente", Toast.LENGTH_SHORT).show()
                    sessionManager.saveSession(updatedUsuario.id, updatedUsuario.nombre)
                    finish()
                }
            }
        }

        binding.btnLogout.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Cerrar Sesión")
                .setMessage("¿Está seguro que desea cerrar sesión?")
                .setPositiveButton("Sí") { _, _ ->
                    sessionManager.clearSession()
                    val intent = Intent(this, LoginActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish()
                }
                .setNegativeButton("No", null)
                .show()
        }
    }

    private fun setupObservers() {
        viewModel.usuario.observe(this) { usuario ->
            if (usuario != null) {
                binding.etNombre.setText(usuario.nombre)
                binding.etApellido.setText(usuario.apellido)
                binding.etEmail.setText(usuario.email)
                binding.etEdad.setText(usuario.edad.toString())
                binding.etTelefono.setText(usuario.telefono)
                
                val enfermedadesArray = resources.getStringArray(com.upn.myoyichan.R.array.enfermedades_array)
                val position = enfermedadesArray.indexOf(usuario.enfermedad)
                if (position >= 0) {
                    binding.spEnfermedad.setSelection(position)
                }
            }
        }
    }
}
