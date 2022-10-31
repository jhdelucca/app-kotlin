package com.example.appteste.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "configuracao")
data class Configuracao(
    @PrimaryKey(autoGenerate = false) val host:String,
    @ColumnInfo(name = "porta") val porta:Int,
    @ColumnInfo(name = "seguranca") val seguranca:Boolean
)
