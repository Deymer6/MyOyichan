package com.upn.myoyichan.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "signos_vitales")
data class SignoVitalEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val usuarioId: Int,
    val tipo: String,       // "PRESION", "GLUCOSA", "PESO", "TEMPERATURA"
    val valor: String,      // "120/80", "95", "70.5", "36.6"
    val unidad: String,     // "mmHg", "mg/dL", "kg", "°C"
    val fecha: String,
    val hora: String,
    val notas: String
)
