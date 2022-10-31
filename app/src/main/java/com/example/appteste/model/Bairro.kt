package com.example.appteste.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
class Bairro(val codigo:Int , val descricao:String, val filial:Int = 1): Parcelable { }