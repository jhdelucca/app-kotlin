package com.example.appteste.model

class UnidadePro(var unidade:String,
                 var qtdeUnidade:Int,
                 var estoqueDisponivel:Double,
                 var precoVenda:Double) {

    override fun toString(): String {
        return "${unidade}/${qtdeUnidade}"
    }
}