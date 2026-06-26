package com.upn.myoyichan.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "medicamentos")
data class MedicamentoEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val usuarioId: Int,
    val nombre: String,
    val dosis: String,
    val frecuencia: String, // "cada 8 horas", "una vez al día", etc.
    val horario: String,    // "08:00", "14:00", "20:00"
    val notas: String,
    val activo: Boolean = true,
    val fechaCreacion: String
)
