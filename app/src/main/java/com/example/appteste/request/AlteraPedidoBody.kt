package com.example.appteste.request

import android.os.Parcelable
import com.example.appteste.request.ItensBody
import kotlinx.android.parcel.Parcelize

@Parcelize
class AlteraPedidoBody(val filialCodigo:Int,
                       val seriePedido:String,
                       val numeroPedido:Int,
                       val numeroOrigem:String,
                       val clienteCodigo: Int,
                       val valorLiquido:Double,
                       val dataEmissao:String = "14/10/2022",
                       val horaEmissao:String = "14:29:10",
                       val items:List<ItensBody>,
                       val formaPagamentoCodigo:String = "DH",
                       val condicaoPagamentoCodigo: String = "1",
                       val naturezaOperacao:String = "VIN",
                       val valorFrete: Double = 0.00,
                       val observacao:String = "TESTE KOTLIN",
                       val valorDesconto:Double = 0.00,
                       val vendedorCodigo:Int = 6,
                       val tipoEntrega:String = "BALCAO",
                       val observacaoFiscal1:String= "",
                       val observacaoFiscal2:String= "",
                       val observacaoFiscal3:String= ""):Parcelable {}