package com.example.appteste.model

class Cliente(val codigo:Int,
              val fantasia:String = "" ,
              val cpfCnpj:String = "",
              val razao:String = fantasia ,
              val limite:Double = 0.00,
              val situacao:String = "",
              val bloqueado:Int = 0) {
}