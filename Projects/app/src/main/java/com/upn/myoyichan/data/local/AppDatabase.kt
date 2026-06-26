package com.upn.myoyichan.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.upn.myoyichan.data.local.dao.ContactoSosDao
import com.upn.myoyichan.data.local.dao.MedicamentoDao
import com.upn.myoyichan.data.local.dao.SignoVitalDao
import com.upn.myoyichan.data.local.dao.UsuarioDao
import com.upn.myoyichan.data.local.entity.ContactoSosEntity
import com.upn.myoyichan.data.local.entity.MedicamentoEntity
import com.upn.myoyichan.data.local.entity.SignoVitalEntity
import com.upn.myoyichan.data.local.entity.UsuarioEntity

@Database(
    entities = [
        UsuarioEntity::class,
        MedicamentoEntity::class,
        SignoVitalEntity::class,
        ContactoSosEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun usuarioDao(): UsuarioDao
    abstract fun medicamentoDao(): MedicamentoDao
    abstract fun signoVitalDao(): SignoVitalDao
    abstract fun contactoSosDao(): ContactoSosDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                // Cargar librerías de SQLCipher antes de cualquier operación
                net.sqlcipher.database.SQLiteDatabase.loadLibs(context)

                val passphrase = "MyOyichanSuperSecretKey2026!".toByteArray()
                val factory = net.sqlcipher.database.SupportFactory(passphrase)

                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "myoyichan_encrypted.db"
                )
                .openHelperFactory(factory)
                .fallbackToDestructiveMigration() // Agregamos esto para manejar cambios de esquema durante desarrollo
                .build()
                
                INSTANCE = instance
                instance
            }
        }
    }
}
