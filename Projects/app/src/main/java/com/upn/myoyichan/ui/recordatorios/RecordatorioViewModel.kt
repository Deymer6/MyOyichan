package com.upn.myoyichan.ui.recordatorios

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.upn.myoyichan.data.local.entity.MedicamentoEntity
import com.upn.myoyichan.repository.MedicamentoRepository
import kotlinx.coroutines.launch

class RecordatorioViewModel(private val repository: MedicamentoRepository) : ViewModel() {

    private val _operationResult = MutableLiveData<Result<Unit>>()
    val operationResult: LiveData<Result<Unit>> = _operationResult

    fun getRecordatorios(userId: Int): LiveData<List<MedicamentoEntity>> {
        return repository.getByUsuarioId(userId)
    }

    fun toggleActivo(medicamento: MedicamentoEntity, isActive: Boolean) {
        viewModelScope.launch {
            try {
                val updated = medicamento.copy(activo = isActive)
                repository.update(updated)
                _operationResult.value = Result.success(Unit)
            } catch (e: Exception) {
                _operationResult.value = Result.failure(e)
            }
        }
    }
}
