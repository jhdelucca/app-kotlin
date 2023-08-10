package com.example.appteste.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.R
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.appteste.adapter.RecyclerViewCarrinhoAdapter
import com.example.appteste.api.Endpoint
import com.example.appteste.database.AppDataBase
import com.example.appteste.database.ItensPedido
import com.example.appteste.database.PedidoUnico
import com.example.appteste.databinding.ActivityCarrinhoBinding
import com.example.appteste.extensions.*
import com.example.appteste.model.*
import com.example.appteste.util.preferences.SharedPreferences
import com.example.appteste.request.AlteraPedidoBody
import com.example.appteste.request.ItensBody
import com.example.appteste.request.UnidadeProRequest
import com.example.appteste.request.InserePedidoBody
import com.example.appteste.util.NetworkUtils
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import kotlinx.android.synthetic.main.activity_carrinho.*
import kotlinx.android.synthetic.main.activity_produto.*
import kotlinx.coroutines.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.ResponseBody
import org.json.JSONArray
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

@OptIn(DelicateCoroutinesApi::class)
class CarrinhoActivity : AppCompatActivity() , RecyclerViewCarrinhoAdapter.ClickBtns {

    private lateinit var binding: ActivityCarrinhoBinding
    private lateinit var appDataBase: AppDataBase
    var listaItens:MutableList<ItensPedido> = mutableListOf()
    private var valorTotal:Double = 0.00
    var listaNat:List<NatOper> = listOf()
    var listaForma:List<FormaPagto> = listOf()
    var listaCond: List<CondPagto> = listOf()
    private var proCodigo:Int = 0
    private  var pedidoUnico: PedidoUnico = PedidoUnico(0)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCarrinhoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        appDataBase = AppDataBase.getDatabase(this)

        binding.idListaCarrinho.layoutManager = LinearLayoutManager(this)
        binding.idListaCarrinho.setHasFixedSize(true)

                getPedido()
                somaValor()
                findByItens()


        binding.btnSalvar.setOnClickListener {
            inserepedidos()
        }

        binding.btnAddItens.setOnClickListener {
            val intent = Intent(this, ProdutoActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            startActivity(intent)

        }

        binding.btnCancelar.setOnClickListener {
            val builder = AlertDialog.Builder(this)
                .setTitle("Deseja Cancelar o Pedido? O mesmo não será gravado.")
                .setNegativeButton("Não") { dialog, which ->
                    dialog.dismiss()
                }
                .setPositiveButton("Sim") { dialog, which ->
                    delete()
                    STATUS = "I"
                    finalizaActivity()
                }

            builder.show()

        }

        binding.btSalvarCar.setOnClickListener {
            if(binding.editValorCar.text.isEmpty() || binding.editQuantidadeCar.text.isEmpty()) {
            Toast.makeText(baseContext, "Quantidade ou valor zerados", Toast.LENGTH_SHORT).show()
        }else{

            alteraItem(binding.editQuantidadeCar.text.toString().toDouble(), binding.editValorCar.text.toString().toDouble(),
            proCodigo, binding.spinUnidadeCar.selectedItem.toString().split("/")[0],
                binding.spinUnidadeCar.selectedItem.toString().split("/")[1].toInt())

                binding.layoutDados.visibility = View.VISIBLE
                binding.layoutAltera.visibility = View.GONE
            }
        }

        binding.btCancelCar.setOnClickListener {
            binding.layoutDados.visibility = View.VISIBLE
            binding.layoutAltera.visibility = View.GONE
        }
    }

    private fun getPedido() {
        GlobalScope.launch(Dispatchers.IO) {
            val pedido = appDataBase.pedidoDao().findPedido()
            verificaAlteracao(pedido)
        }
    }

    @Suppress("SENSELESS_COMPARISON")
    private suspend  fun verificaAlteracao(pedido: PedidoUnico) {
        withContext(Dispatchers.Main) {
            pedidoUnico = if(pedido == null) PedidoUnico(0) else pedido
            listaSpinners(pedidoUnico)
        }
    }

    private fun listaSpinners(pedido: PedidoUnico) {
        getNatureza(pedido)
        getForma(pedido)
        getCondicao(pedido)
        val listaVend = listOf<String>("Joao" , "Felipe" , "Sammy")
        binding.spinVendedor.adapter = ArrayAdapter(applicationContext, R.layout.support_simple_spinner_dropdown_item,listaVend)
        binding.spinVendedor.setSelection(0)
    }

    private fun getNatureza(pedido: PedidoUnico) {
        val retrofitCliente = NetworkUtils.getRetrofitInstance(URL_PRINCIPAL)
        val endpoint = retrofitCliente.create(Endpoint::class.java)
     //   val teste = pedidoUnico

        val callback = endpoint.getNatureza(pedido.natCodigo , FILIAL)

        callback.enqueue(object : Callback<ResponseBody> {
            @SuppressLint("SuspiciousIndentation")
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                var data = response.body()?.string()

                if (data == null) {
                    data = response.errorBody()?.string()
                }


                if(response.isSuccessful) {
                    val json = JSONArray(data!!)
                    listaNat  = GsonBuilder().create().fromJson(json.toString(), Array<NatOper>::class.java).toList()
                    if(listaNat.isNotEmpty()) {
                      binding.spinNat.adapter = ArrayAdapter(applicationContext, R.layout.support_simple_spinner_dropdown_item , listaNat)
                        binding.spinNat.setSelection(0)
                    }else{
                        Toast.makeText(baseContext, "Natureza nao encontrada", Toast.LENGTH_LONG).show()
                    }
                }else{
                    Toast.makeText(baseContext, JSONObject(data!!).getString("message"), Toast.LENGTH_LONG).show()
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Toast.makeText(baseContext, "Erro de Serviço!!", Toast.LENGTH_LONG).show()
            }
        })

        binding.spinNat.isEnabled = STATUS != "A"

    }

   private fun getForma(pedido: PedidoUnico) {
        val retrofitCliente = NetworkUtils.getRetrofitInstance(URL_PRINCIPAL)
        val endpoint = retrofitCliente.create(Endpoint::class.java)
        val callback = endpoint.getForma(CODIGO_CLIENTE,pedido.fpgCodigo)

        callback.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                var data = response.body()?.string()

                if (data == null) {
                    data = response.errorBody()?.string()
                }

                if(response.isSuccessful) {
                    val json = JSONArray(data!!)
                    listaForma  = GsonBuilder().create().fromJson(json.toString(), Array<FormaPagto>::class.java).toList()
                    if(listaForma.isNotEmpty()) {
                        binding.spinForma.adapter = ArrayAdapter(applicationContext, R.layout.support_simple_spinner_dropdown_item , listaForma)
                        binding.spinForma.setSelection(0)
                    }else{
                        Toast.makeText(baseContext, "Natureza nao encontrada", Toast.LENGTH_LONG).show()
                    }
                }else{
                    Toast.makeText(baseContext, JSONObject(data!!).getString("message"), Toast.LENGTH_LONG).show()
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Toast.makeText(baseContext, "Erro de Serviço!!", Toast.LENGTH_LONG).show()
            }
        })

    }

  private fun getCondicao(pedido: PedidoUnico) {
        val retrofitCliente = NetworkUtils.getRetrofitInstance(URL_PRINCIPAL)
        val endpoint = retrofitCliente.create(Endpoint::class.java)
        val callback = endpoint.getCondicao(CODIGO_CLIENTE,pedido.cpgCodigo)

        callback.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                var data = response.body()?.string()

                if (data == null) {
                    data = response.errorBody()?.string()

                }


                if(response.isSuccessful) {
                    val json = JSONArray(data!!)
                    listaCond  = GsonBuilder().create().fromJson(json.toString(), Array<CondPagto>::class.java).toList()
                    if(listaCond.isNotEmpty()) {
                        binding.spinCondicao.adapter = ArrayAdapter(applicationContext, R.layout.support_simple_spinner_dropdown_item , listaCond)
                        binding.spinCondicao.setSelection(0)
                    }else{
                        Toast.makeText(baseContext, "Natureza nao encontrada", Toast.LENGTH_LONG).show()
                    }
                }else{
                    Toast.makeText(baseContext, JSONObject(data!!).getString("message"), Toast.LENGTH_LONG).show()
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Toast.makeText(baseContext, "Erro de Serviço!!", Toast.LENGTH_LONG).show()
            }
        })

    }

    @OptIn(DelicateCoroutinesApi::class)
    private fun findByItens()  {
        GlobalScope.launch(Dispatchers.IO) {
            val recebeLista = appDataBase.itenspedidoDao().findByAll()
            preencheLista(recebeLista)
          //  listaItens.set(0,recebeLista.get(0))
        }
    }

    private fun preencheLista(lista:MutableList<ItensPedido>) {
        runOnUiThread {
            listaItens = lista
            binding.idListaCarrinho.adapter = RecyclerViewCarrinhoAdapter(applicationContext, listaItens, this)

        }
    }

    @SuppressLint("SetTextI18n")
    private fun somaValor() {
        GlobalScope.launch(Dispatchers.IO) {
            val recebeValor = appDataBase.itenspedidoDao().somaValor()
            setTotal(recebeValor)

        }
    }

    private suspend fun setTotal(recebeValor: Double) {
        withContext(Dispatchers.Main) {
            valorTotal = recebeValor
            val format = NumberFormat.getCurrencyInstance(Locale("pt", "br"))
            binding.txtTotal.text = format.format(valorTotal)
        }
    }

   private  fun inserepedidos() {

        val retrofitClient = NetworkUtils.getRetrofitInstance("http://192.168.0.13:8080/vendasguardian/")

        val endpoint = retrofitClient.create(Endpoint::class.java)
        //val natureza = binding.spinNat.selectedItem.toString().split("-")
        val natureza = binding.spinNat.adapter.getItem(binding.spinNat.selectedItemPosition) as NatOper
        val forma = binding.spinForma.adapter.getItem(binding.spinForma.selectedItemPosition) as FormaPagto
        val condicao = binding.spinCondicao.adapter.getItem(binding.spinCondicao.selectedItemPosition) as CondPagto
        val itensBody = percorreListaItens()
        //val authorization = "SPACE $TOKEN"
        val authorization = "SPACE ${SharedPreferences(applicationContext).getString("token")}"

       val callback = if(STATUS != "A") {
           val insereBody = InserePedidoBody(transformaHora(),CODIGO_CLIENTE,valorTotal,transformaData(),transformaHora(),itensBody,
               forma.formaCodigo, condicao.condicaoCodigo, natureza.natCodigo)
           val teste = Gson().toJson(insereBody)
           val body: RequestBody = Gson().toJson(insereBody).toRequestBody("application/json;charset=utf-8".toMediaType())
           for(item:ItensPedido in listaItens) {
               if (item.precovenda < item.precotab) {
                   startActivity(Intent(applicationContext,AutorizacaoActivity::class.java).putExtra("body" , insereBody))
                   return
               }
               }
           endpoint.inserePedidos(authorization,body)
       }else{
           val alterarBody = AlteraPedidoBody(FILIAL, pedidoUnico.serie , pedidoUnico.numero,pedidoUnico.origem,
               listaItens[0].cliente, valorTotal,transformaData(),transformaHora(),itensBody,
               forma.formaCodigo, condicao.condicaoCodigo, natureza.natCodigo)
           for(item:ItensPedido in listaItens) {
               if (item.precovenda < item.precotab) {
                   startActivity(Intent(applicationContext,AutorizacaoActivity::class.java).putExtra("body" , alterarBody))
                   return
               }
           }
           val body:RequestBody = Gson().toJson(alterarBody).toRequestBody("application/json;charset=utf-8".toMediaType())
           endpoint.alteraPedidos(authorization,body)

       }
       binding.progressbar.visibility = View.VISIBLE

        callback.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                var data = response.body()?.string()

                if (data == null) {
                    data = response.errorBody()?.string()
                }
                val json = JSONObject(data!!)

                if(response.isSuccessful) {
                    binding.progressbar.visibility = View.GONE
                    if(json.getBoolean("sucesso")) {
                        if(STATUS == "A") {
                            Toast.makeText(baseContext, "Pedido Alterado", Toast.LENGTH_LONG).show()
                            deletePedidoAlterar()
                        } else {
                            Toast.makeText(baseContext, "Pedido Inserido", Toast.LENGTH_LONG).show()
                        }
                        delete()
                        binding.spinNat.isEnabled = true
                        STATUS = "I"
                        finalizaActivity()
                    }else{
                        Toast.makeText(baseContext, json.getString("mensagemUsuario"), Toast.LENGTH_LONG).show()
                    }
                }else{
                    binding.progressbar.visibility = View.GONE
                    Toast.makeText(baseContext, json.getString("mensagemUsuario"), Toast.LENGTH_LONG).show()
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                binding.progressbar.visibility = View.GONE
                Toast.makeText(baseContext, "Erro Servidor", Toast.LENGTH_LONG).show()
            }
        })

    }

    private fun delete() {
        GlobalScope.launch(Dispatchers.IO) {
            appDataBase.itenspedidoDao().delete()
        }
    }

    private fun percorreListaItens():List<ItensBody> {
        val listaItensBody: MutableList<ItensBody> = mutableListOf()
        for(itens in listaItens) {
            listaItensBody.add(
                ItensBody(produtoCodigo = itens.procodigo,
                quantidade = itens.quantidade , valorLiquido = itens.quantidade * itens.precovenda,
            valorUnitario = itens.precovenda , unidade = itens.unpunidade , unidadeQuantidade = itens.unpquant)
            )
        }
        return listaItensBody
    }

    @SuppressLint("SimpleDateFormat")
    private fun transformaData(): String {
        val dataHoraAtual = Date()
        return SimpleDateFormat("dd/MM/yyyy").format(dataHoraAtual)
    }

    @SuppressLint("SimpleDateFormat")
    fun transformaHora(): String {
        val dataHoraAtual = Date()
        return SimpleDateFormat("HH:mm:ss").format(dataHoraAtual)
    }

     fun finalizaActivity() {
            val intent = Intent(this, TesteActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            startActivity(intent)
    }

    override fun editItem(itensPedido: ItensPedido) {
        proCodigo = itensPedido.procodigo
        binding.layoutDados.visibility = View.GONE
        binding.layoutAltera.visibility = View.VISIBLE
        listarUnidadePro(proCodigo , itensPedido.unpunidade , itensPedido.unpquant)
        binding.editQuantidadeCar.setText(itensPedido.quantidade.toString())
        binding.editValorCar.setText((itensPedido.precovenda).toString())


    }

    override fun excluirItens(itensPedido: ItensPedido) {
        GlobalScope.launch(Dispatchers.IO) {
            appDataBase.itenspedidoDao().deleteItemUnico(itensPedido.procodigo,itensPedido.unpunidade,itensPedido.unpquant)
            listaItens.remove(itensPedido)
            somaValor()
        }
    }

    private fun deletePedidoAlterar() {
        GlobalScope.launch(Dispatchers.IO) {
            appDataBase.pedidoDao().delete()
        }
    }

    private fun alteraItem(quantidade:Double, precovenda:Double,procodigo:Int,unpunidade:String,unpquant:Int) {
        GlobalScope.launch(Dispatchers.IO) {
            appDataBase.itenspedidoDao().editItem(quantidade,precovenda, procodigo ,unpunidade ,unpquant)
            findByItens()
            somaValor()
        }

    }

    private fun listarUnidadePro(codigoProduto:Int , unidade:String , undquant:Int) {
        val retrofitClient = NetworkUtils.getRetrofitInstance(URL_PRINCIPAL)
        val endpoint = retrofitClient.create(Endpoint::class.java)
        val unidadePro = UnidadeProRequest(FILIAL,codigoProduto,unidade,undquant)
        val body: RequestBody = Gson().toJson(unidadePro).toRequestBody("application/json".toMediaType())
        var listUnidadePro:List<UnidadePro> = listOf()

        val callback = endpoint.getUnidadePro(body)

        callback.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                var data = response.body()?.string()

                if (data == null) {
                    data = response.errorBody()?.string()
                }

                if (response.isSuccessful) {
                    if(JSONObject(data!!).getBoolean("resposta")) {
                        listUnidadePro = GsonBuilder().create().fromJson(JSONObject(data).getJSONArray("dados").toString(), Array<UnidadePro>::class.java).toList()
                        binding.spinUnidadeCar.adapter = ArrayAdapter(applicationContext, R.layout.support_simple_spinner_dropdown_item , listUnidadePro)
                        binding.editEstoquCar.setText(listUnidadePro[0].estoqueDisponivel.toString())
                    }else{
                        Toast.makeText(baseContext, JSONObject(data).getString("mensagemUsuario"), Toast.LENGTH_LONG).show()
                        binding.spinUnidadeCar.adapter = ArrayAdapter(applicationContext, R.layout.support_simple_spinner_dropdown_item , listUnidadePro)
                    }
                } else {
                    Toast.makeText(baseContext, JSONObject(data!!).getString("message"), Toast.LENGTH_LONG).show()
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Toast.makeText(baseContext, "Erro ao carregar Unidade", Toast.LENGTH_LONG).show()
            }
        })
    }
}