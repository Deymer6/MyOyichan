package com.upn.myoyichan.repository

import androidx.lifecycle.LiveData
import com.upn.myoyichan.data.local.dao.SignoVitalDao
import com.upn.myoyichan.data.local.entity.SignoVitalEntity

class SignoVitalRepository(private val signoVitalDao: SignoVitalDao) {

    fun getByUsuarioId(userId: Int): LiveData<List<SignoVitalEntity>> =
        signoVitalDao.getByUsuarioId(userId)

    fun getByUsuarioIdAndTipo(userId: Int, tipo: String): LiveData<List<SignoVitalEntity>> =
        signoVitalDao.getByUsuarioIdAndTipo(userId, tipo)

    suspend fun getById(id: Int): SignoVitalEntity? = signoVitalDao.getById(id)

    suspend fun insert(signoVital: SignoVitalEntity): Long = signoVitalDao.insert(signoVital)

    suspend fun update(signoVital: SignoVitalEntity) = signoVitalDao.update(signoVital)

    suspend fun delete(signoVital: SignoVitalEntity) = signoVitalDao.delete(signoVital)
}
