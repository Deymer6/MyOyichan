package com.upn.myoyichan.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.upn.myoyichan.repository.ContactoSosRepository
import com.upn.myoyichan.repository.MedicamentoRepository
import com.upn.myoyichan.repository.SignoVitalRepository
import com.upn.myoyichan.repository.UsuarioRepository
import com.upn.myoyichan.ui.contactos.ContactoViewModel
import com.upn.myoyichan.ui.medicamentos.MedicamentoViewModel
import com.upn.myoyichan.ui.recordatorios.RecordatorioViewModel
import com.upn.myoyichan.ui.vitales.VitalViewModel
import com.upn.myoyichan.ui.auth.AuthViewModel

class ViewModelFactory(
    private val usuarioRepository: UsuarioRepository? = null,
    private val medicamentoRepository: MedicamentoRepository? = null,
    private val signoVitalRepository: SignoVitalRepository? = null,
    private val contactoSosRepository: ContactoSosRepository? = null
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AuthViewModel::class.java)) {
            return AuthViewModel(usuarioRepository!!) as T
        }
        if (modelClass.isAssignableFrom(MedicamentoViewModel::class.java)) {
            return MedicamentoViewModel(medicamentoRepository!!) as T
        }
        if (modelClass.isAssignableFrom(VitalViewModel::class.java)) {
            return VitalViewModel(signoVitalRepository!!) as T
        }
        if (modelClass.isAssignableFrom(ContactoViewModel::class.java)) {
            return ContactoViewModel(contactoSosRepository!!) as T
        }
        if (modelClass.isAssignableFrom(RecordatorioViewModel::class.java)) {
            return RecordatorioViewModel(medicamentoRepository!!) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
