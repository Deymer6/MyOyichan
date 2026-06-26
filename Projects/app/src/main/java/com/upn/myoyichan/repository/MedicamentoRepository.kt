package com.upn.myoyichan.repository

import androidx.lifecycle.LiveData
import com.upn.myoyichan.data.local.dao.MedicamentoDao
import com.upn.myoyichan.data.local.entity.MedicamentoEntity

class MedicamentoRepository(private val medicamentoDao: MedicamentoDao) {

    fun getByUsuarioId(userId: Int): LiveData<List<MedicamentoEntity>> =
        medicamentoDao.getByUsuarioId(userId)

    fun getActivosByUsuarioId(userId: Int): LiveData<List<MedicamentoEntity>> =
        medicamentoDao.getActivosByUsuarioId(userId)

    suspend fun getById(id: Int): MedicamentoEntity? = medicamentoDao.getById(id)

    suspend fun countActivos(userId: Int): Int = medicamentoDao.countActivos(userId)

    suspend fun insert(medicamento: MedicamentoEntity): Long = medicamentoDao.insert(medicamento)

    suspend fun update(medicamento: MedicamentoEntity) = medicamentoDao.update(medicamento)

    suspend fun delete(medicamento: MedicamentoEntity) = medicamentoDao.delete(medicamento)
}
