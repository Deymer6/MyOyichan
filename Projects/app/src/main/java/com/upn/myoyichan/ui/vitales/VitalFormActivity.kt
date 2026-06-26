package com.upn.myoyichan.ui.vitales

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import com.upn.myoyichan.data.local.AppDatabase
import com.upn.myoyichan.databinding.ActivityVitalFormBinding
import com.upn.myoyichan.repository.SignoVitalRepository
import com.upn.myoyichan.ui.BaseActivity
import com.upn.myoyichan.ui.ViewModelFactory
import com.upn.myoyichan.utils.SessionManager
import java.util.Calendar

class VitalFormActivity : BaseActivity() {

    private lateinit var binding: ActivityVitalFormBinding
    private lateinit var viewModel: VitalViewModel
    private lateinit var sessionManager: SessionManager
    
    private var selectedDate = ""
    private var selectedTime = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVitalFormBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)
        val repository = SignoVitalRepository(AppDatabase.getInstance(this).signoVitalDao())
        val factory = ViewModelFactory(signoVitalRepository = repository)
        viewModel = ViewModelProvider(this, factory)[VitalViewModel::class.java]

        val tipoPredefinido = intent.getStringExtra("tipo_vital")
        
        setupViews(tipoPredefinido)
        setupListeners()
        setupObservers()
    }

    private fun setupViews(tipoPredefinido: String?) {
        val calendar = Calendar.getInstance()
        selectedDate = String.format("%04d-%02d-%02d", calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH) + 1, calendar.get(Calendar.DAY_OF_MONTH))
        selectedTime = String.format("%02d:%02d", calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE))
        
        binding.btnFecha.text = selectedDate
        binding.btnHora.text = selectedTime

        if (tipoPredefinido != null) {
            val tipos = resources.getStringArray(com.upn.myoyichan.R.array.tipos_vitales_array)
            val pos = tipos.indexOf(tipoPredefinido)
            if (pos >= 0) binding.spTipo.setSelection(pos)
        }
    }

    private fun setupListeners() {
        binding.btnBack.setOnClickListener { finish() }

        binding.btnFecha.setOnClickListener {
            val cal = Calendar.getInstance()
            DatePickerDialog(this, { _, year, month, dayOfMonth ->
                selectedDate = String.format("%04d-%02d-%02d", year, month + 1, dayOfMonth)
                binding.btnFecha.text = selectedDate
            }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
        }

        binding.btnHora.setOnClickListener {
            val cal = Calendar.getInstance()
            TimePickerDialog(this, { _, hourOfDay, minute ->
                selectedTime = String.format("%02d:%02d", hourOfDay, minute)
                binding.btnHora.text = selectedTime
            }, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), true).show()
        }

        binding.btnSave.setOnClickListener {
            val tipo = binding.spTipo.selectedItem.toString()
            val valor = binding.etValor.text.toString().trim()
            val notas = binding.etNotas.text.toString().trim()

            if (valor.isEmpty()) {
                binding.tilValor.error = "Requerido"
                return@setOnClickListener
            }

            val unidad = when (tipo) {
                "PRESION" -> "mmHg"
                "GLUCOSA" -> "mg/dL"
                "PESO" -> "kg"
                "TEMPERATURA" -> "°C"
                else -> ""
            }

            viewModel.saveVital(
                userId = sessionManager.getUsuarioId(),
                tipo = tipo,
                valor = valor,
                unidad = unidad,
                fecha = selectedDate,
                hora = selectedTime,
                notas = notas
            )
        }
    }

    private fun setupObservers() {
        viewModel.operationResult.observe(this) { result ->
            result.onSuccess {
                Toast.makeText(this, "Registro guardado", Toast.LENGTH_SHORT).show()
                finish()
            }.onFailure { exception ->
                Toast.makeText(this, "Error: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
