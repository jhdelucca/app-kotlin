package com.example.appteste.model

class CondPagto(val condicaoCodigo:String,
                val condicaoDesc:String) {
    override fun toString(): String {
        return "${condicaoCodigo} - ${condicaoDesc}"
    }
}