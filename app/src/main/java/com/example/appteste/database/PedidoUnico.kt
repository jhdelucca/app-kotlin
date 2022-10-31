package com.example.appteste.database

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.android.parcel.Parcelize

@Entity(tableName = "pedidounico")
data class PedidoUnico(
    @PrimaryKey
    @ColumnInfo(name = "numero")
    val numero:Int ,
    @ColumnInfo(name = "serie")
    val serie:String= "",
    @ColumnInfo(name = "origem")
    val origem:String="",
    @ColumnInfo(name = "natureza")
    val natCodigo:String = "" ,
    @ColumnInfo(name = "formapagto")
    val fpgCodigo:String="" ,
    @ColumnInfo(name = "condpagto")
    val cpgCodigo:Int = 0) { }