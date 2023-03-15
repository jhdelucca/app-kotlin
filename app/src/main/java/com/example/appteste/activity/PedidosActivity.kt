package com.example.appteste.activity

import android.app.DatePickerDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.PopupMenu
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.Toolbar
import com.example.appteste.adapter.ListaPedidosAdapter
import com.example.appteste.R
import com.example.appteste.api.Endpoint
import com.example.appteste.database.AppDataBase
import com.example.appteste.database.ItensPedido
import com.example.appteste.databinding.ActivityPedidosBinding
import com.example.appteste.extensions.*
import com.example.appteste.database.PedidoUnico
import com.example.appteste.model.Pedidos
import com.example.appteste.request.PedidoUnicoRequest
import com.example.appteste.request.PedidosRequest
import com.example.appteste.util.NetworkUtils
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import kotlinx.android.synthetic.main.activity_pedidos.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*

class PedidosActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPedidosBinding
    var listaPedidos: List<Pedidos> = listOf()
    var listaItens : List<ItensPedido> = listOf()
    lateinit var pedidoUnico: PedidoUnico
    private lateinit var appDataBase: AppDataBase


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPedidosBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val singToolbar: Toolbar = findViewById(R.id.toolBar)
        setSupportActionBar(singToolbar)
        supportActionBar?.title = null

        appDataBase = AppDataBase.getDatabase(this)

        val dataHoje = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Calendar.getInstance().time)
        listarPedidos(dataHoje, dataHoje)

        binding.editTextDate.setText(
            SimpleDateFormat(
                "dd/MM/yyyy",
                Locale.getDefault()
            ).format(Calendar.getInstance().time)
        )
        binding.editTextDate2.setText(
            SimpleDateFormat(
                "dd/MM/yyyy",
                Locale.getDefault()
            ).format(Calendar.getInstance().time)
        )

        binding.editTextDate.setOnClickListener {
            val calendar = Calendar.getInstance()
            DatePickerDialog(
                this, { view, year, month, dayOfMonth ->
                    calendar.set(Calendar.YEAR, year)
                    calendar.set(Calendar.MONTH, month)
                    calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                    binding.editTextDate.setText(
                        SimpleDateFormat(
                            "dd/MM/yyyy",
                            Locale.getDefault()
                        ).format(calendar.time)
                    )
                    binding.editTextDate.setFocusable(false)
                    val dataInicial = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.time)
                    val dataFinal = binding.editTextDate2.text.split(("/"))
                    listarPedidos(dataInicial, "${dataFinal.get(2)}-${dataFinal.get(1)}-${dataFinal.get(0)}")
                },
                calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)
            ).show()

        }

        binding.editTextDate2.setOnClickListener {
            val calendar = Calendar.getInstance()
            DatePickerDialog(
                this, DatePickerDialog.OnDateSetListener { view, year, month, dayOfMonth ->
                    calendar.set(Calendar.YEAR, year)
                    calendar.set(Calendar.MONTH, month)
                    calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                    binding.editTextDate2.setText(
                        SimpleDateFormat(
                            "dd/MM/yyyy",
                            Locale.getDefault()
                        ).format(calendar.time)
                    )
                    binding.editTextDate2.setFocusable(false)
                    val dataFinal = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.time)
                    val dataInicial = binding.editTextDate.text.split(("/"))
                    listarPedidos("${dataInicial.get(2)}-${dataInicial.get(1)}-${dataInicial.get(0)}", dataFinal)
                },
                calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        binding.idListaPedidos.setOnItemClickListener { parent, view, position, id ->
            val popupMenu = PopupMenu(baseContext, view)
            popupMenu.menuInflater.inflate(R.menu.popup_menu,popupMenu.menu)

            popupMenu.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.alterar_pedido -> {
                        GlobalScope.launch(Dispatchers.IO) {
                            val recebeLista = appDataBase.itenspedidoDao().findByAll()
                            verificaLista(recebeLista,position)
                        }
                        true
                    }
                    R.id.visualizar_pedido -> {
                        val params = Bundle()
                        params.putInt("numero" , listaPedidos[position].numero)
                        params.putString("serie" , listaPedidos[position].serie)
                        startActivity(Intent(this,ReportPedidoActivity::class.java).putExtras(params))
                        true
                    }
                    else -> false
                }
            }
            popupMenu.show()
        }
    }

    suspend fun verificaLista(lista:MutableList<ItensPedido> , position:Int) {
        withContext(Dispatchers.Main) {
            if(lista.isEmpty()) {
                getPedidoUnico(position)
            }
        }
    }

    fun getPedidoUnico(position: Int) {
        val retrofitClient = NetworkUtils.getRetrofitInstance(URL_PRINCIPAL)
        val endpoint = retrofitClient.create(Endpoint::class.java)

        val pedidos = PedidoUnicoRequest(FILIAL,listaPedidos.get(position).serie,listaPedidos.get(position).numero)

        val body: RequestBody = Gson().toJson(pedidos).toRequestBody("application/json".toMediaType())
        val callback = endpoint.getPedidosUnico(body)

        callback.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                var data = response.body()?.string()

                if (data == null) {
                    data = response.errorBody()?.string()
                }
                //val json = JSONArray(data!!)
                // val dados = JSONObject(data!!).getJSONArray("dados")

                if (response.isSuccessful) {
                    if(JSONObject(data!!).getBoolean("resposta")) {
                        listaItens =  GsonBuilder().create().fromJson(JSONObject(data).getJSONArray("dados").toString(), Array<ItensPedido>::class.java).toList()
                        pedidoUnico = GsonBuilder().create().fromJson(JSONObject(data).getJSONArray("dados").toString(), Array<PedidoUnico>::class.java).toList().get(0)
                        inserirItens(listaItens)

                    }else{
                        Toast.makeText(baseContext, JSONObject(data).getString("mensagemUsuario"), Toast.LENGTH_LONG).show()
                        listaItens = listOf()
                    }
                } else {
                    Toast.makeText(baseContext, JSONObject(data!!).getString("message"), Toast.LENGTH_LONG).show()
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Toast.makeText(baseContext, "Errado!!!!", Toast.LENGTH_LONG).show()
            }
        })
    }
    private fun inserirItens(lista:List<ItensPedido>) {

        for(itens:ItensPedido in lista) {
            val itensPedido = ItensPedido(procodigo = itens.procodigo, prodesc = itens.prodesc, unpunidade = itens.unpunidade, unpquant = itens.unpquant,
                quantidade = itens.quantidade, precovenda = itens.precovenda , precotab = itens.precotab , cliente = itens.cliente, status = 1)

            GlobalScope.launch(Dispatchers.IO) {
                appDataBase.itenspedidoDao().insert(itensPedido)

            }
        }
        inserePedidoAlterar()
    }

    fun inserePedidoAlterar() {
        GlobalScope.launch(Dispatchers.IO) {
            appDataBase.pedidoDao().delete()
            appDataBase.pedidoDao().insert(pedidoUnico)
        }
        STATUS = "A"
        startActivity(Intent(this, CarrinhoActivity::class.java))
    }

    private fun delete() {
        GlobalScope.launch(Dispatchers.IO) {
            appDataBase.itenspedidoDao().delete()
        }
    }
    fun listarPedidos(dataInicial:String, dataFinal:String) {
        val retrofitClient = NetworkUtils.getRetrofitInstance(URL_PRINCIPAL)
        val endpoint = retrofitClient.create(Endpoint::class.java)
        val pedidos = if(DADOS_USUARIO.getInt("supVenda") == 1 || DADOS_USUARIO.getInt("genVenda") == 1) {
            PedidosRequest(FILIAL, dataInicial, dataFinal);
        }else{
            PedidosRequest(FILIAL, dataInicial, dataFinal, DADOS_USUARIO.getInt("codigo"))
        }

        val body: RequestBody = Gson().toJson(pedidos).toRequestBody("application/json".toMediaType())
        val callback = endpoint.getPedidos(body)

        callback.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                var data = response.body()?.string()

                if (data == null) {
                    data = response.errorBody()?.string()
                }
                   //val json = JSONArray(data!!)
                   // val dados = JSONObject(data!!).getJSONArray("dados")

                if (response.isSuccessful) {
                    if(JSONObject(data!!).getBoolean("resposta")) {
                            listaPedidos =  GsonBuilder().create().fromJson(JSONObject(data).getJSONArray("dados").toString(), Array<Pedidos>::class.java).toList()
                            binding.idListaPedidos.adapter = ListaPedidosAdapter(applicationContext, listaPedidos)
                        }else{
                        Toast.makeText(baseContext, JSONObject(data).getString("mensagemUsuario"), Toast.LENGTH_LONG).show()
                        listaPedidos = listOf()
                        binding.idListaPedidos.adapter = ListaPedidosAdapter(applicationContext, listaPedidos)
                    }
                } else {
                    Toast.makeText(baseContext, "${JSONObject(data!!).getString("message")}\"", Toast.LENGTH_LONG).show()
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Toast.makeText(baseContext, "Errado!!!!", Toast.LENGTH_LONG).show()
            }
        })

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.menu_pedidos,menu)

        return true

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle item selection
        return when (item.itemId) {
            R.id.menuClientes -> {
                val intent = Intent(this, TesteActivity::class.java)
                startActivity(intent)
                true
            }
            R.id.menuHelp -> {

                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}