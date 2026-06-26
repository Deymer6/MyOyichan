package com.upn.myoyichan.data.local.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.upn.myoyichan.data.local.entity.UsuarioEntity

@Dao
interface UsuarioDao {

    @Insert
    suspend fun insert(usuario: UsuarioEntity): Long

    @Update
    suspend fun update(usuario: UsuarioEntity)

    @Delete
    suspend fun delete(usuario: UsuarioEntity)

    @Query("SELECT * FROM usuarios WHERE id = :id")
    suspend fun getById(id: Int): UsuarioEntity?

    @Query("SELECT * FROM usuarios WHERE email = :email LIMIT 1")
    suspend fun getByEmail(email: String): UsuarioEntity?

    @Query("SELECT * FROM usuarios WHERE email = :email AND password = :passwordHash LIMIT 1")
    suspend fun login(email: String, passwordHash: String): UsuarioEntity?

    @Query("SELECT * FROM usuarios")
    fun getAll(): LiveData<List<UsuarioEntity>>
}
