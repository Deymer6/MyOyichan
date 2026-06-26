package com.upn.myoyichan.repository

import androidx.lifecycle.LiveData
import com.upn.myoyichan.data.local.dao.ContactoSosDao
import com.upn.myoyichan.data.local.entity.ContactoSosEntity

class ContactoSosRepository(private val contactoSosDao: ContactoSosDao) {

    fun getByUsuarioId(userId: Int): LiveData<List<ContactoSosEntity>> =
        contactoSosDao.getByUsuarioId(userId)

    suspend fun getById(id: Int): ContactoSosEntity? = contactoSosDao.getById(id)

    suspend fun getPrincipal(userId: Int): ContactoSosEntity? =
        contactoSosDao.getPrincipal(userId)

    suspend fun insert(contacto: ContactoSosEntity): Long {
        if (contacto.esPrincipal) {
            contactoSosDao.clearPrincipal(contacto.usuarioId)
        }
        return contactoSosDao.insert(contacto)
    }

    suspend fun update(contacto: ContactoSosEntity) {
        if (contacto.esPrincipal) {
            contactoSosDao.clearPrincipal(contacto.usuarioId)
        }
        contactoSosDao.update(contacto)
    }

    suspend fun delete(contacto: ContactoSosEntity) = contactoSosDao.delete(contacto)
}
