package com.example.appteste.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query


@Dao
interface ConfigDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(configuracao: Configuracao)

    @Query("SELECT * FROM CONFIGURACAO LIMIT 1")
    fun findByAll():Configuracao

    @Query("DELETE FROM CONFIGURACAO")
    fun delete()
}