package com.upn.myoyichan.ui.contactos

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.upn.myoyichan.data.local.entity.ContactoSosEntity
import com.upn.myoyichan.repository.ContactoSosRepository
import kotlinx.coroutines.launch

class ContactoViewModel(private val repository: ContactoSosRepository) : ViewModel() {

    private val _operationResult = MutableLiveData<Result<Unit>>()
    val operationResult: LiveData<Result<Unit>> = _operationResult

    private val _contacto = MutableLiveData<ContactoSosEntity?>()
    val contacto: LiveData<ContactoSosEntity?> = _contacto

    fun getContactos(userId: Int): LiveData<List<ContactoSosEntity>> {
        return repository.getByUsuarioId(userId)
    }

    fun getContactoById(id: Int) {
        viewModelScope.launch {
            _contacto.value = repository.getById(id)
        }
    }

    fun saveContacto(
        id: Int = 0,
        userId: Int,
        nombre: String,
        relacion: String,
        telefono: String,
        esPrincipal: Boolean
    ) {
        viewModelScope.launch {
            try {
                val entity = ContactoSosEntity(
                    id = id,
                    usuarioId = userId,
                    nombre = nombre,
                    relacion = relacion,
                    telefono = telefono,
                    esPrincipal = esPrincipal
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

    fun deleteContacto(entity: ContactoSosEntity) {
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
