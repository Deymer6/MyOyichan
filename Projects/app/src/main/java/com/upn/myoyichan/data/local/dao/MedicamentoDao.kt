package com.upn.myoyichan.data.local.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.upn.myoyichan.data.local.entity.MedicamentoEntity

@Dao
interface MedicamentoDao {

    @Insert
    suspend fun insert(medicamento: MedicamentoEntity): Long

    @Update
    suspend fun update(medicamento: MedicamentoEntity)

    @Delete
    suspend fun delete(medicamento: MedicamentoEntity)

    @Query("SELECT * FROM medicamentos WHERE id = :id")
    suspend fun getById(id: Int): MedicamentoEntity?

    @Query("SELECT * FROM medicamentos WHERE usuarioId = :userId ORDER BY horario ASC")
    fun getByUsuarioId(userId: Int): LiveData<List<MedicamentoEntity>>

    @Query("SELECT * FROM medicamentos WHERE usuarioId = :userId AND activo = 1 ORDER BY horario ASC")
    fun getActivosByUsuarioId(userId: Int): LiveData<List<MedicamentoEntity>>

    @Query("SELECT COUNT(*) FROM medicamentos WHERE usuarioId = :userId AND activo = 1")
    suspend fun countActivos(userId: Int): Int
}
