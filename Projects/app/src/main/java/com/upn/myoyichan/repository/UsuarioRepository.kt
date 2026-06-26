package com.upn.myoyichan.repository

import androidx.lifecycle.LiveData
import com.upn.myoyichan.data.local.dao.UsuarioDao
import com.upn.myoyichan.data.local.entity.UsuarioEntity

class UsuarioRepository(private val usuarioDao: UsuarioDao) {

    fun getAll(): LiveData<List<UsuarioEntity>> = usuarioDao.getAll()

    suspend fun getById(id: Int): UsuarioEntity? = usuarioDao.getById(id)

    suspend fun getByEmail(email: String): UsuarioEntity? = usuarioDao.getByEmail(email)

    suspend fun login(email: String, passwordHash: String): UsuarioEntity? =
        usuarioDao.login(email, passwordHash)

    suspend fun insert(usuario: UsuarioEntity): Long = usuarioDao.insert(usuario)

    suspend fun update(usuario: UsuarioEntity) = usuarioDao.update(usuario)

    suspend fun delete(usuario: UsuarioEntity) = usuarioDao.delete(usuario)
}
