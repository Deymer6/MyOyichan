package com.upn.myoyichan.data.local.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.upn.myoyichan.data.local.entity.SignoVitalEntity

@Dao
interface SignoVitalDao {

    @Insert
    suspend fun insert(signoVital: SignoVitalEntity): Long

    @Update
    suspend fun update(signoVital: SignoVitalEntity)

    @Delete
    suspend fun delete(signoVital: SignoVitalEntity)

    @Query("SELECT * FROM signos_vitales WHERE id = :id")
    suspend fun getById(id: Int): SignoVitalEntity?

    @Query("SELECT * FROM signos_vitales WHERE usuarioId = :userId ORDER BY fecha DESC, hora DESC")
    fun getByUsuarioId(userId: Int): LiveData<List<SignoVitalEntity>>

    @Query("SELECT * FROM signos_vitales WHERE usuarioId = :userId AND tipo = :tipo ORDER BY fecha DESC, hora DESC")
    fun getByUsuarioIdAndTipo(userId: Int, tipo: String): LiveData<List<SignoVitalEntity>>
}
