package com.example.appteste.model

class NatOper(val natCodigo:String,
              val natDesc:String) {

    override fun toString(): String {
        return "${natCodigo} - ${natDesc}"
    }
}