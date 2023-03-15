package com.example.appteste.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.appteste.adapter.ItensAutorizarAdapter
import com.example.appteste.api.Endpoint
import com.example.appteste.database.AppDataBase
import com.example.appteste.database.ItensPedido
import com.example.appteste.databinding.ActivityAutorizacaoBinding
import com.example.appteste.extensions.*
import com.example.appteste.model.*
import com.example.appteste.request.AlteraPedidoBody
import com.example.appteste.request.InserePedidoBody
import com.example.appteste.util.NetworkUtils
import com.google.gson.Gson
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AutorizacaoActivity : AppCompatActivity() {
    lateinit var binding: ActivityAutorizacaoBinding
    lateinit var insereBody:InserePedidoBody
    lateinit var alteraBody:AlteraPedidoBody
    lateinit var appDataBase:AppDataBase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAutorizacaoBinding.inflate(layoutInflater)
        setContentView(binding.root)
        appDataBase = AppDataBase.getDatabase(this)

        findItensAutorizar()
        verifiicaStatus()

        binding.btnAutorizar.setOnClickListener {
            getColaborador()
        }

    }

    private fun verifiicaStatus() {
        runOnUiThread {
            if (STATUS == "A") {
                alteraBody = intent.extras?.getParcelable<AlteraPedidoBody>("body")!!
            } else {
                insereBody = intent.extras?.getParcelable<InserePedidoBody>("body")!!
            }
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    private fun findItensAutorizar()  {
        GlobalScope.launch(Dispatchers.IO) {
            val recebeLista = appDataBase.itenspedidoDao().selectItensAutorizar()
            preencheLista(recebeLista)
        }
    }

    private fun preencheLista(lista:List<ItensPedido>) {
        runOnUiThread {
            binding.listAutorizar.adapter = ItensAutorizarAdapter(applicationContext,lista)
        }
    }

    fun getColaborador() {
        val retrofitCliente = NetworkUtils.getRetrofitInstance(URL_PRINCIPAL)
        val endpoint = retrofitCliente.create(Endpoint::class.java)
        val usuario = binding.autorizaUser.text.toString()
        val callback = endpoint.getColaborador(usuario)

        callback.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                var data = response.body()?.string()

                if (data == null) {
                    data = response.errorBody()?.string()
                }
                val json = JSONObject(data!!)

                if(response.isSuccessful) {
                    if(json.getBoolean("resposta")) {
                        Login()
                        }else{
                        Toast.makeText(baseContext, json.getString("mensagemUsuario"), Toast.LENGTH_SHORT).show()
                    }
                }else{
                    Toast.makeText(baseContext, json.getString("message"), Toast.LENGTH_SHORT).show()

                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Toast.makeText(baseContext, "Erro de Serviço!!", Toast.LENGTH_LONG).show()
            }
        })
    }

    fun Login() {
        val retrofitClient = NetworkUtils.getRetrofitInstance("http://192.168.0.13:8080/vendasguardian/")
        val endpoint = retrofitClient.create(Endpoint::class.java)


        val user = User(binding.autorizaUser.text.toString(),binding.autorizarSenha.text.toString(), FILIAL)

        val body: RequestBody = Gson().toJson(user).toRequestBody("application/json".toMediaType())
        val callback = endpoint.authenticate(body)

        callback.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                var data = response.body()?.string()

                if (data == null) {
                    data = response.errorBody()?.string()
                }
                val json = JSONObject(data!!)

                if(response.isSuccessful) {
                    inserepedidos()
                }else{
                    Toast.makeText(baseContext, json.getString("mensagemUsuario"), Toast.LENGTH_LONG).show()
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Toast.makeText(baseContext, "Errado!!!!", Toast.LENGTH_LONG).show()
            }
        })
    }

    private  fun inserepedidos() {
        val retrofitClient = NetworkUtils.getRetrofitInstance("http://192.168.0.13:8080/vendasguardian/")

        val endpoint = retrofitClient.create(Endpoint::class.java)
        val authorization = "SPACE $TOKEN"

        val callback = if(STATUS != "A") {
            val body: RequestBody = Gson().toJson(insereBody).toRequestBody("application/json;charset=utf-8".toMediaType())
            endpoint.inserePedidos(authorization,body)
        }else{
            val body:RequestBody = Gson().toJson(alteraBody).toRequestBody("application/json;charset=utf-8".toMediaType())
            endpoint.alteraPedidos(authorization,body)
        }

        callback.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                var data = response.body()?.string()

                if (data == null) {
                    data = response.errorBody()?.string()
                }
                val json = JSONObject(data!!)

                if(response.isSuccessful) {
                    if(json.getBoolean("sucesso")) {
                        if(STATUS == "A") {
                            Toast.makeText(baseContext, "Pedido Alterado", Toast.LENGTH_LONG).show()
                            deletePedidoAlterar()
                        } else {
                            Toast.makeText(baseContext, "Pedido Inserido", Toast.LENGTH_LONG).show()
                        }
                        delete()
                        STATUS = "I"
                        finalizaActivity()
                    }else{
                        Toast.makeText(baseContext, json.getString("mensagemUsuario"), Toast.LENGTH_LONG).show()
                    }
                }else{
                    Toast.makeText(baseContext, json.getString("mensagemUsuario"), Toast.LENGTH_LONG).show()
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Toast.makeText(baseContext, "Erro Servidor", Toast.LENGTH_LONG).show()
            }
        })

    }

    @OptIn(DelicateCoroutinesApi::class)
    private fun deletePedidoAlterar() {
        GlobalScope.launch(Dispatchers.IO) {
            appDataBase.pedidoDao().delete()
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    private fun delete() {
        GlobalScope.launch(Dispatchers.IO) {
            appDataBase.itenspedidoDao().delete()
        }
    }

    fun finalizaActivity() {
        val intent = Intent(this, TesteActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        startActivity(intent)
    }




}