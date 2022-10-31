package com.example.appteste.model

class FormaPagto(val formaCodigo:String,
                 val formaDesc:String) {

    override fun toString(): String {
        return "${formaCodigo} - ${formaDesc}"
    }
}