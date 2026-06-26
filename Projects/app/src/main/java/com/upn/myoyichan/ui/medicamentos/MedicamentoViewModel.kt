package com.upn.myoyichan.ui.medicamentos

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.upn.myoyichan.data.local.entity.MedicamentoEntity
import com.upn.myoyichan.repository.MedicamentoRepository
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MedicamentoViewModel(private val repository: MedicamentoRepository) : ViewModel() {

    private val _medicamento = MutableLiveData<MedicamentoEntity?>()
    val medicamento: LiveData<MedicamentoEntity?> = _medicamento

    private val _operationResult = MutableLiveData<Result<Unit>>()
    val operationResult: LiveData<Result<Unit>> = _operationResult

    fun getMedicamentos(userId: Int): LiveData<List<MedicamentoEntity>> {
        return repository.getByUsuarioId(userId)
    }

    fun getMedicamentoById(id: Int) {
        viewModelScope.launch {
            _medicamento.value = repository.getById(id)
        }
    }

    fun saveMedicamento(
        id: Int = 0,
        userId: Int,
        nombre: String,
        dosis: String,
        frecuencia: String,
        horario: String,
        notas: String
    ) {
        viewModelScope.launch {
            try {
                val fecha = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                val entity = MedicamentoEntity(
                    id = id,
                    usuarioId = userId,
                    nombre = nombre,
                    dosis = dosis,
                    frecuencia = frecuencia,
                    horario = horario,
                    notas = notas,
                    fechaCreacion = fecha
                )
                if (id == 0) {
                    repository.insert(entity)
                } else {
                    repository.update(entity)
                }
                _operationResult.value = Result.success(Unit)
            } catch (e: Exception) {
                _operationResult.value = Result.failure(e)
            }
        }
    }

    fun deleteMedicamento(medicamentoEntity: MedicamentoEntity) {
        viewModelScope.launch {
            try {
                repository.delete(medicamentoEntity)
                _operationResult.value = Result.success(Unit)
            } catch (e: Exception) {
                _operationResult.value = Result.failure(e)
            }
        }
    }
}
