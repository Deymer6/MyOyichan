package com.upn.myoyichan.ui.vitales

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.upn.myoyichan.data.local.entity.SignoVitalEntity
import com.upn.myoyichan.repository.SignoVitalRepository
import kotlinx.coroutines.launch

class VitalViewModel(private val repository: SignoVitalRepository) : ViewModel() {

    private val _operationResult = MutableLiveData<Result<Unit>>()
    val operationResult: LiveData<Result<Unit>> = _operationResult

    fun getVitales(userId: Int, tipo: String): LiveData<List<SignoVitalEntity>> {
        return if (tipo == "TODOS") {
            repository.getByUsuarioId(userId)
        } else {
            repository.getByUsuarioIdAndTipo(userId, tipo)
        }
    }

    fun saveVital(
        userId: Int,
        tipo: String,
        valor: String,
        unidad: String,
        fecha: String,
        hora: String,
        notas: String
    ) {
        viewModelScope.launch {
            try {
                val entity = SignoVitalEntity(
                    usuarioId = userId,
                    tipo = tipo,
                    valor = valor,
                    unidad = unidad,
                    fecha = fecha,
                    hora = hora,
                    notas = notas
                )
                repository.insert(entity)
                _operationResult.value = Result.success(Unit)
            } catch (e: Exception) {
                _operationResult.value = Result.failure(e)
            }
        }
    }

    fun deleteVital(entity: SignoVitalEntity) {
        viewModelScope.launch {
            try {
                repository.delete(entity)
                _operationResult.value = Result.success(Unit)
            } catch (e: Exception) {
                _operationResult.value = Result.failure(e)
            }
        }
    }
}
