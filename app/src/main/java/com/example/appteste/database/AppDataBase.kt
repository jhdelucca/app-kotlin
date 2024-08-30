package com.example.appteste.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase


@Database(entities = [Configuracao :: class , ItensPedido :: class , PedidoUnico :: class], version = 1)
abstract class AppDataBase : RoomDatabase() {

    abstract fun configDao(): ConfigDao
    abstract fun itenspedidoDao() : ItensPedidoDao
    abstract fun pedidoDao():PedidosUnicoDao

    companion object {
        @Volatile
        private var INSTANCE: AppDataBase? = null
        private var teste1 = 10

        fun getDatabase(context: Context): AppDataBase {

            val tempInstance = INSTANCE
            if (tempInstance != null) {
                return tempInstance
            }
            synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDataBase::class.java,
                    "app_database"
                ).build()
                INSTANCE = instance
                return instance
            }
        }
    }
}