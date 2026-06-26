package com.upn.myoyichan.data.local.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.upn.myoyichan.data.local.entity.ContactoSosEntity

@Dao
interface ContactoSosDao {

    @Insert
    suspend fun insert(contacto: ContactoSosEntity): Long

    @Update
    suspend fun update(contacto: ContactoSosEntity)

    @Delete
    suspend fun delete(contacto: ContactoSosEntity)

    @Query("SELECT * FROM contactos_sos WHERE id = :id")
    suspend fun getById(id: Int): ContactoSosEntity?

    @Query("SELECT * FROM contactos_sos WHERE usuarioId = :userId ORDER BY esPrincipal DESC, nombre ASC")
    fun getByUsuarioId(userId: Int): LiveData<List<ContactoSosEntity>>

    @Query("SELECT * FROM contactos_sos WHERE usuarioId = :userId AND esPrincipal = 1 LIMIT 1")
    suspend fun getPrincipal(userId: Int): ContactoSosEntity?

    @Query("UPDATE contactos_sos SET esPrincipal = 0 WHERE usuarioId = :userId")
    suspend fun clearPrincipal(userId: Int)
}
