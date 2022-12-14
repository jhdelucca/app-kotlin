package com.example.appvendas

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView.LayoutManager
import com.example.appvendas.EndPoint.EndPoint
import com.example.appvendas.NetWork.NetWorkUtils
import com.example.appvendas.adapter.ReportAdapter
import com.example.appvendas.databinding.ActivityReportPedidoBinding
import com.example.appvendas.extension.URL_PRINCIPAL
import com.example.appvendas.request.PedidoIndividualRequest
import com.example.appvendas.request.PedidosRequest
import com.example.appvendas.request.ReportPedidoRequest
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ReportPedidoActivity : AppCompatActivity(),ReportAdapter.ClickProduto {

    private lateinit var binding : ActivityReportPedidoBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityReportPedidoBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        val pedido = intent.extras!!.getParcelable<PedidosRequest>("pedido")
        getBuscaPedido(pedido)
    }

    private fun getBuscaPedido(pedido: PedidosRequest?) {
        val retrofitClient = URL_PRINCIPAL?.let { NetWorkUtils.getRetrofitInstance(it) }
        val endPoint = retrofitClient?.create(EndPoint::class.java)

        pedido?.pednumero?.let {
            endPoint?.getBuscaPedidoReport(it)?.enqueue(object : Callback<ResponseBody>{
                override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                    if (response.isSuccessful){
                        val data = response.body()?.string()
                        val type = object : TypeToken<List<ReportPedidoRequest>>(){}.type
                        var respostaReport = Gson().fromJson<List<ReportPedidoRequest>>(data.toString(),type)
                        preencheValores(respostaReport)
                    }
                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    Toast.makeText(baseContext,"Nao Foi Encontrado report de pedido", Toast.LENGTH_LONG).show()
                }

            })
        }

    }
    private fun preencheValores(respostaReport: List<ReportPedidoRequest>) {
        binding.recyclerItemPed.layoutManager = LinearLayoutManager(this)
        binding.recyclerItemPed.setHasFixedSize(true)
        val reportPedido = ReportAdapter(respostaReport,this,this)
        binding.recyclerItemPed.adapter = reportPedido
        binding.recebeCliente.setText(respostaReport[0].pesrazao)
        binding.recebeEndereco.setText(respostaReport[0].enderecocliente)
        binding.recebePagamento.setText(respostaReport[0].fpgpagamento)
        binding.recebeTelefone.setText(respostaReport[0].endfone1)
        binding.recebeUf.setText(respostaReport[0].endufsigla)
        binding.recebeValorPedido.setText(respostaReport[0].pedvalor.toString())
        binding.recebeVendedor.setText(respostaReport[0].clbrazao)
        binding.horaEmissao.setText(respostaReport[0].pedhoracad)
        binding.dtEmissao.setText(respostaReport[0].peddatacad)
    }
    override fun clickProduto(produto: ReportPedidoRequest) {

    }
}