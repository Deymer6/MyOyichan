package com.upn.myoyichan.ui.auth

import android.os.Bundle
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import com.upn.myoyichan.data.local.AppDatabase
import com.upn.myoyichan.databinding.ActivityRegisterBinding
import com.upn.myoyichan.repository.UsuarioRepository
import com.upn.myoyichan.ui.BaseActivity
import com.upn.myoyichan.ui.ViewModelFactory
import android.util.Patterns

class RegisterActivity : BaseActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private lateinit var viewModel: AuthViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val repository = UsuarioRepository(AppDatabase.getInstance(this).usuarioDao())
        val factory = ViewModelFactory(usuarioRepository = repository)
        viewModel = ViewModelProvider(this, factory)[AuthViewModel::class.java]

        configurarSpinnerEnfermedad()
        setupListeners()
        setupObservers()
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

        binding.btnRegister.setOnClickListener {
            val nombre = binding.etNombre.text.toString().trim()
            val apellido = binding.etApellido.text.toString().trim()
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()
            val confirmPassword = binding.etConfirmPassword.text.toString().trim()
            val edadStr = binding.etEdad.text.toString().trim()
            val telefono = binding.etTelefono.text.toString().trim()
            val enfermedad = binding.spEnfermedad.selectedItem.toString()

            var isValid = true

            // Validaciones
            val nameRegex = Regex("^[a-zA-ZáéíóúÁÉÍÓÚñÑ\\s]+\$")
            if (nombre.isEmpty()) { binding.tilNombre.error = "Requerido"; isValid = false } 
            else if (!nameRegex.matches(nombre)) { binding.tilNombre.error = "Solo letras permitidas"; isValid = false }
            else binding.tilNombre.error = null

            if (apellido.isEmpty()) { binding.tilApellido.error = "Requerido"; isValid = false } 
            else if (!nameRegex.matches(apellido)) { binding.tilApellido.error = "Solo letras permitidas"; isValid = false }
            else binding.tilApellido.error = null

            if (email.isEmpty()) { binding.tilEmail.error = "Requerido"; isValid = false } 
            else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) { binding.tilEmail.error = "Formato inválido"; isValid = false }
            else binding.tilEmail.error = null

            if (password.isEmpty()) { binding.tilPassword.error = "Requerido"; isValid = false } else binding.tilPassword.error = null
            if (password != confirmPassword) { binding.tilConfirmPassword.error = "Las contraseñas no coinciden"; isValid = false } else binding.tilConfirmPassword.error = null
            
            val phoneRegex = Regex("^9[0-9]{8}\$")

            if (edadStr.isEmpty()) { binding.tilEdad.error = "Requerido"; isValid = false } 
            else {
                val edadInt = edadStr.toIntOrNull()
                if (edadInt == null) { binding.tilEdad.error = "Solo números"; isValid = false }
                else if (edadInt <= 0 || edadInt > 120) { binding.tilEdad.error = "Edad inválida (1-120)"; isValid = false }
                else binding.tilEdad.error = null
            }

            if (telefono.isEmpty()) { binding.tilTelefono.error = "Requerido"; isValid = false } 
            else if (!phoneRegex.matches(telefono)) { binding.tilTelefono.error = "Debe tener 9 dígitos y empezar con 9"; isValid = false }
            else binding.tilTelefono.error = null

            if (enfermedad == "Seleccione su enfermedad" || enfermedad.isEmpty()) {
                Toast.makeText(this, "Seleccione una enfermedad", Toast.LENGTH_SHORT).show()
                isValid = false
            }

            if (isValid) {
                val edad = edadStr.toInt()
                viewModel.register(nombre, apellido, email, password, edad, enfermedad, telefono)
            }
        }
    }

    private fun setupObservers() {
        viewModel.registerResult.observe(this) { result ->
            result.onSuccess {
                Toast.makeText(this, "Cuenta creada exitosamente", Toast.LENGTH_SHORT).show()
                finish()
            }.onFailure { exception ->
                Toast.makeText(this, exception.message, Toast.LENGTH_SHORT).show()
            }
        }
    }
}
