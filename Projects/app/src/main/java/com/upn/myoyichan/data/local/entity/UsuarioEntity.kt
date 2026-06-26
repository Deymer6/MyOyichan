package com.upn.myoyichan.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "usuarios")
data class UsuarioEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val nombre: String,
    val apellido: String,
    val email: String,
    val password: String, // SHA-256 hash
    val edad: Int,
    val enfermedad: String,
    val telefono: String,
    val fechaRegistro: String
)
