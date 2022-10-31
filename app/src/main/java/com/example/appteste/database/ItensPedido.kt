package com.example.appteste.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity(tableName = "itenspedido")
data class ItensPedido (
    @PrimaryKey(autoGenerate = true)
    val sequencial:Int = 0,
    @ColumnInfo(name = "procodigo")
    val procodigo:Int ,
    @ColumnInfo(name = "prodesc")
    val prodesc:String,
    @ColumnInfo(name = "unpunidade")
    val unpunidade:String,
    @ColumnInfo(name = "unpquant")
    val unpquant:Int,
    @ColumnInfo(name = "quantidade")
    val quantidade:Double,
    @ColumnInfo(name = "precovenda")
    val precovenda:Double,
    @ColumnInfo(name = "precotab")
    val precotab :Double,
    @ColumnInfo(name = "cliente")
    val cliente :Int,
    @ColumnInfo(name = "status")
    val status :Int = 0)




