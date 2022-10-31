package com.example.appteste.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class User(
    @SerializedName("login")
    val login:String,
    @SerializedName("senha")
    val senha:String,
    @SerializedName("filialCodigo")
    val filialCodigo:Int): Parcelable {



}