package com.example.appteste.util.preferences

import android.content.Context
import android.content.SharedPreferences
import okhttp3.ResponseBody
import retrofit2.Callback

class SharedPreferences(context: Context) {

    private val token : SharedPreferences = context.getSharedPreferences("token" , Context.MODE_PRIVATE)

    fun storeString(key:String,str:String) {
        token.edit().putString(key,str).apply()
    }

    fun getString(key: String):String {
        return token.getString(key,"") ?: ""
    }
}