package com.upn.myoyichan.ui.contactos

import android.os.Bundle
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import com.upn.myoyichan.data.local.AppDatabase
import com.upn.myoyichan.databinding.ActivityContactoFormBinding
import com.upn.myoyichan.repository.ContactoSosRepository
import com.upn.myoyichan.ui.BaseActivity
import com.upn.myoyichan.ui.ViewModelFactory
import com.upn.myoyichan.utils.SessionManager

class ContactoFormActivity : BaseActivity() {

    private lateinit var binding: ActivityContactoFormBinding
    private lateinit var viewModel: ContactoViewModel
    private lateinit var sessionManager: SessionManager
    private var contactoId: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityContactoFormBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)
        val repository = ContactoSosRepository(AppDatabase.getInstance(this).contactoSosDao())
        val factory = ViewModelFactory(contactoSosRepository = repository)
        viewModel = ViewModelProvider(this, factory)[ContactoViewModel::class.java]

        contactoId = intent.getIntExtra("CONTACTO_ID", 0)

        setupListeners()
        setupObservers()

        if (contactoId != 0) {
            viewModel.getContactoById(contactoId)
        }
    }

    private fun setupListeners() {
        binding.btnBack.setOnClickListener { finish() }

        binding.btnSave.setOnClickListener {
            val nombre = binding.etNombre.text.toString().trim()
            val telefono = binding.etTelefono.text.toString().trim()
            val relacion = binding.spRelacion.selectedItem.toString()
            val esPrincipal = binding.switchPrincipal.isChecked

            var isValid = true
            val nameRegex = Regex("^[a-zA-ZáéíóúÁÉÍÓÚñÑ\\s]+\$")
            val phoneRegex = Regex("^9[0-9]{8}\$")

            if (nombre.isEmpty()) { binding.tilNombre.error = "Requerido"; isValid = false } 
            else if (!nameRegex.matches(nombre)) { binding.tilNombre.error = "Solo letras permitidas"; isValid = false }
            else binding.tilNombre.error = null

            if (telefono.isEmpty()) { binding.tilTelefono.error = "Requerido"; isValid = false } 
            else if (!phoneRegex.matches(telefono)) { binding.tilTelefono.error = "Debe tener 9 dígitos y empezar con 9"; isValid = false }
            else binding.tilTelefono.error = null

            if (isValid) {
                viewModel.saveContacto(
                    id = contactoId,
                    userId = sessionManager.getUsuarioId(),
                    nombre = nombre,
                    relacion = relacion,
                    telefono = telefono,
                    esPrincipal = esPrincipal
                )
            }
        }
    }

    private fun setupObservers() {
        viewModel.contacto.observe(this) { contacto ->
            if (contacto != null) {
                binding.etNombre.setText(contacto.nombre)
                binding.etTelefono.setText(contacto.telefono)
                binding.switchPrincipal.isChecked = contacto.esPrincipal

                val relaciones = resources.getStringArray(com.upn.myoyichan.R.array.relaciones_array)
                val pos = relaciones.indexOf(contacto.relacion)
                if (pos >= 0) binding.spRelacion.setSelection(pos)
            }
        }

        viewModel.operationResult.observe(this) { result ->
            result.onSuccess {
                Toast.makeText(this, "Contacto guardado", Toast.LENGTH_SHORT).show()
                finish()
            }.onFailure { exception ->
                Toast.makeText(this, "Error: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
