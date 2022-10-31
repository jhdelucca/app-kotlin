package com.example.appteste.request

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class UserRequest (@SerializedName("token") val token:String,
                        @SerializedName("usuarioNome") val usuarioNome:String,
                        @SerializedName("login") val login:String,
                        @SerializedName("filialCodigo") val filialCodigo:Int): Parcelable { }
