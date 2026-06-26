package com.upn.myoyichan.ui.medicamentos

import android.app.AlarmManager
import android.app.PendingIntent
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import com.upn.myoyichan.data.local.AppDatabase
import com.upn.myoyichan.databinding.ActivityMedicamentoFormBinding
import com.upn.myoyichan.repository.MedicamentoRepository
import com.upn.myoyichan.ui.BaseActivity
import com.upn.myoyichan.ui.ViewModelFactory
import com.upn.myoyichan.utils.AlarmReceiver
import com.upn.myoyichan.utils.SessionManager
import java.util.Calendar

class MedicamentoFormActivity : BaseActivity() {

    private lateinit var binding: ActivityMedicamentoFormBinding
    private lateinit var viewModel: MedicamentoViewModel
    private lateinit var sessionManager: SessionManager
    private var medicamentoId: Int = 0
    private var selectedHour = 8
    private var selectedMinute = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMedicamentoFormBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)
        val repository = MedicamentoRepository(AppDatabase.getInstance(this).medicamentoDao())
        val factory = ViewModelFactory(medicamentoRepository = repository)
        viewModel = ViewModelProvider(this, factory)[MedicamentoViewModel::class.java]

        medicamentoId = intent.getIntExtra("MEDICAMENTO_ID", 0)

        setupListeners()
        setupObservers()

        if (medicamentoId != 0) {
            viewModel.getMedicamentoById(medicamentoId)
        } else {
            updateTimeButtonText()
        }
    }

    private fun setupListeners() {
        binding.btnBack.setOnClickListener { finish() }

        binding.btnHora.setOnClickListener {
            TimePickerDialog(
                this,
                { _, hourOfDay, minute ->
                    selectedHour = hourOfDay
                    selectedMinute = minute
                    updateTimeButtonText()
                },
                selectedHour,
                selectedMinute,
                true
            ).show()
        }

        binding.btnSave.setOnClickListener {
            val nombre = binding.etNombre.text.toString().trim()
            val dosis = binding.etDosis.text.toString().trim()
            val frecuencia = binding.spFrecuencia.selectedItem.toString()
            val notas = binding.etNotas.text.toString().trim()
            val horario = String.format("%02d:%02d", selectedHour, selectedMinute)

            if (nombre.isEmpty()) {
                binding.tilNombre.error = "Requerido"
                return@setOnClickListener
            }

            viewModel.saveMedicamento(
                id = medicamentoId,
                userId = sessionManager.getUsuarioId(),
                nombre = nombre,
                dosis = dosis,
                frecuencia = frecuencia,
                horario = horario,
                notas = notas
            )
        }
    }

    private fun setupObservers() {
        viewModel.medicamento.observe(this) { medicamento ->
            if (medicamento != null) {
                binding.etNombre.setText(medicamento.nombre)
                binding.etDosis.setText(medicamento.dosis)
                binding.etNotas.setText(medicamento.notas)

                val partesHora = medicamento.horario.split(":")
                if (partesHora.size == 2) {
                    selectedHour = partesHora[0].toInt()
                    selectedMinute = partesHora[1].toInt()
                    updateTimeButtonText()
                }

                val frecuencias = resources.getStringArray(com.upn.myoyichan.R.array.frecuencias_array)
                val pos = frecuencias.indexOf(medicamento.frecuencia)
                if (pos >= 0) binding.spFrecuencia.setSelection(pos)
            }
        }

        viewModel.operationResult.observe(this) { result ->
            result.onSuccess {
                Toast.makeText(this, "Medicamento guardado", Toast.LENGTH_SHORT).show()
                programarAlarma()
                finish()
            }.onFailure { exception ->
                Toast.makeText(this, "Error: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateTimeButtonText() {
        binding.btnHora.text = String.format("%02d:%02d", selectedHour, selectedMinute)
    }

    private fun programarAlarma() {
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(this, AlarmReceiver::class.java).apply {
            putExtra(AlarmReceiver.EXTRA_MEDICAMENTO_NOMBRE, binding.etNombre.text.toString())
            putExtra(AlarmReceiver.EXTRA_NOTIFICATION_ID, medicamentoId.takeIf { it != 0 } ?: System.currentTimeMillis().toInt())
        }
        
        val pendingIntent = PendingIntent.getBroadcast(
            this,
            medicamentoId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val calendar = Calendar.getInstance().apply {
            timeInMillis = System.currentTimeMillis()
            set(Calendar.HOUR_OF_DAY, selectedHour)
            set(Calendar.MINUTE, selectedMinute)
            set(Calendar.SECOND, 0)
        }

        if (calendar.timeInMillis <= System.currentTimeMillis()) {
            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }

        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            pendingIntent
        )
    }
}
