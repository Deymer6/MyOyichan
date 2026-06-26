package com.upn.myoyichan.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "contactos_sos")
data class ContactoSosEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val usuarioId: Int,
    val nombre: String,
    val relacion: String,   // "Hijo/a", "Médico", "Familiar", "Otro"
    val telefono: String,
    val esPrincipal: Boolean = false
)
