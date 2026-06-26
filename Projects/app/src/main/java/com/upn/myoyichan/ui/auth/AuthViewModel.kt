package com.upn.myoyichan.ui.auth

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.upn.myoyichan.data.local.entity.UsuarioEntity
import com.upn.myoyichan.repository.UsuarioRepository
import com.upn.myoyichan.utils.PasswordUtils
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AuthViewModel(private val repository: UsuarioRepository) : ViewModel() {

    private val _loginResult = MutableLiveData<Result<UsuarioEntity>>()
    val loginResult: LiveData<Result<UsuarioEntity>> = _loginResult

    private val _registerResult = MutableLiveData<Result<Long>>()
    val registerResult: LiveData<Result<Long>> = _registerResult

    private val _recoverResult = MutableLiveData<Result<Unit>>()
    val recoverResult: LiveData<Result<Unit>> = _recoverResult

    private val _usuario = MutableLiveData<UsuarioEntity?>()
    val usuario: LiveData<UsuarioEntity?> = _usuario

    fun login(email: String, passwordRaw: String) {
        viewModelScope.launch {
            try {
                val hash = PasswordUtils.hashPassword(passwordRaw)
                val user = repository.login(email, hash)
                if (user != null) {
                    _loginResult.value = Result.success(user)
                } else {
                    _loginResult.value = Result.failure(Exception("Credenciales incorrectas"))
                }
            } catch (e: Exception) {
                _loginResult.value = Result.failure(e)
            }
        }
    }

    fun register(
        nombre: String,
        apellido: String,
        email: String,
        passwordRaw: String,
        edad: Int,
        enfermedad: String,
        telefono: String
    ) {
        viewModelScope.launch {
            try {
                val existing = repository.getByEmail(email)
                if (existing != null) {
                    _registerResult.value = Result.failure(Exception("Email ya registrado"))
                    return@launch
                }
                
                val hash = PasswordUtils.hashPassword(passwordRaw)
                val fecha = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                
                val nuevoUsuario = UsuarioEntity(
                    nombre = nombre,
                    apellido = apellido,
                    email = email,
                    password = hash,
                    edad = edad,
                    enfermedad = enfermedad,
                    telefono = telefono,
                    fechaRegistro = fecha
                )
                
                val id = repository.insert(nuevoUsuario)
                _registerResult.value = Result.success(id)
            } catch (e: Exception) {
                _registerResult.value = Result.failure(e)
            }
        }
    }

    fun recoverPassword(email: String, newPasswordRaw: String) {
        viewModelScope.launch {
            try {
                val existing = repository.getByEmail(email)
                if (existing != null) {
                    val hash = PasswordUtils.hashPassword(newPasswordRaw)
                    val updated = existing.copy(password = hash)
                    repository.update(updated)
                    _recoverResult.value = Result.success(Unit)
                } else {
                    _recoverResult.value = Result.failure(Exception("Email no encontrado"))
                }
            } catch (e: Exception) {
                _recoverResult.value = Result.failure(e)
            }
        }
    }

    fun getUsuarioById(id: Int) {
        viewModelScope.launch {
            _usuario.value = repository.getById(id)
        }
    }

    fun updateProfile(usuarioEntity: UsuarioEntity) {
        viewModelScope.launch {
            repository.update(usuarioEntity)
            _usuario.value = usuarioEntity
        }
    }
}
