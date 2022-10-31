package com.example.appteste.request

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
class ItensBody(
    val produtoCodigo:Int,
    val quantidade:Double,
    val valorLiquido:Double,
    val valorUnitario: Double,
    val valorDesconto:Double = 0.00,
    val unidade:String,
    val unidadeQuantidade:Int):Parcelable { }