package com.example.appteste.activity

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.example.appteste.adapter.ListaAdapter
import com.example.appteste.R
import com.example.appteste.api.Endpoint
import com.example.appteste.database.AppDataBase
import com.example.appteste.database.ItensPedido
import com.example.appteste.extensions.*
import com.example.appteste.model.Cliente
import com.example.appteste.request.UserRequest
import com.example.appteste.util.NetworkUtils
import com.google.gson.GsonBuilder
import kotlinx.android.synthetic.main.layout_teste.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class TesteActivity : AppCompatActivity() {

    lateinit var text: TextView
    lateinit var singToolbar: Toolbar
    lateinit var listView: ListView
    var listaCliente: List<Cliente> = listOf()
    var listaPesquisaCliente: List<Cliente> = listOf()
    var flag: Int = 0
    private lateinit var appDataBase: AppDataBase


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.layout_teste)

        appDataBase = AppDataBase.getDatabase(this)
        text = findViewById(R.id.textParams)
        listView = findViewById(R.id.id_listaCli)

        val singToolbar: Toolbar = findViewById(R.id.toolBar)
        setSupportActionBar(singToolbar)
        supportActionBar?.title = null

        // Parametros Com Bundle
        val textoPassado = intent.getStringExtra("texto")
        val textoSenha = intent.getStringExtra("senha")
        val filial = intent.getStringExtra("filial")
        //  text.text = "Bem Vindo ${textoPassado} // Senha: $textoSenha // Filial: $filial>"

        // Parametros com Parcelable
        // val user = intent.extras?.getParcelable<UserRequest>("user")
        //if (user != null) {
        //  text.text = user.login
        text.text = "Bem Vindo: $NOME_USUARIO"
        //text.text = "Bem Vindo: ${DADOS_USUARIO.getString("razao")}"
        //}

        ListarKotlin()
        /** btnPesquisar.setOnClickListener {
        if(editCodigo.text.isEmpty()) {
        ListarKotlin()
        }else{
        pesquisaCodigo()
        }
        }**/

        listView.setOnItemClickListener { parent, view, position, id ->
            /**    val it = Intent(applicationContext,BairroActivity::class.java)
            val bairro = Bairro(listaCliente.get(position).codigo, listaCliente.get(position).fantasia)
            it.putExtra("bairro", bairro) // Tipo Parcelable
            startActivity(it) **/

            if (listaPesquisaCliente.size > 0) {
                CODIGO_CLIENTE = listaPesquisaCliente.get(position).codigo
            } else {
                CODIGO_CLIENTE = listaCliente.get(position).codigo
            }

            findByItens()

        }

        editCodigo.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }

            override fun afterTextChanged(s: Editable?) {
                val textDigitado = s.toString()
                var listaTemporaria: MutableList<Cliente> = mutableListOf()

                for (cliente: Cliente in listaCliente) {
                    if ((cliente.fantasia.contains(textDigitado, true) || cliente.codigo.toString()
                            .contains(textDigitado)) &&
                        textDigitado != ""
                    ) {
                        listaTemporaria.add(cliente)
                    }
                }

                if ((listaTemporaria.isNotEmpty()) || (listaTemporaria.isEmpty() && textDigitado.isNotEmpty())) {
                    listView.adapter = ListaAdapter(applicationContext, listaTemporaria)
                    listaPesquisaCliente = listaTemporaria

                } else {
                    listView.adapter = ListaAdapter(applicationContext, listaCliente)
                    listaPesquisaCliente = listOf()
                }
                //   listView.adapter = ListaAdapter(applicationContext, listaTemporaria)
            }
        })
    }

    private fun findByItens() {
        GlobalScope.launch(Dispatchers.IO) {
            val recebeLista = appDataBase.itenspedidoDao().findByAll()
            verificaPedidoCliente(recebeLista)
        }
    }

    private fun verificaPedidoCliente(lista: List<ItensPedido>) {
        if (lista.size == 0 || lista.get(0).cliente == CODIGO_CLIENTE) {
            startActivity(Intent(applicationContext, ProdutoActivity::class.java))
            STATUS = "I"
        } else {
            runOnUiThread {
                AlertDialog.Builder(this)
                    .setTitle(
                        "Ja existe pedido em digitação para outro cliente. " +
                                "Deseja excluir e iniciar nova?"
                    )
                    .setNegativeButton("Não") { dialog, which ->
                        dialog.dismiss()
                    }
                    .setPositiveButton("Sim") { dialog, which ->
                        delete()
                        STATUS = "I"
                        startActivity(Intent(applicationContext, ProdutoActivity::class.java))
                    }.show()
            }
        }
    }

    private fun delete() {
        GlobalScope.launch(Dispatchers.IO) {
            appDataBase.itenspedidoDao().delete()
        }
    }

    fun Listar() {
        val retrofitClient = NetworkUtils.getRetrofitInstance("http://192.168.0.13:2020/apivendas/")

        val endpoint = retrofitClient.create(Endpoint::class.java)
        val user = intent.extras?.getParcelable<UserRequest>("user")
        val querys: MutableMap<String, String> =
            mutableMapOf("sys" to "AVR", "filial" to user?.filialCodigo.toString(), "usuario" to user?.login.toString())


        val callback = endpoint.listCliente(querys)

        callback.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                var data = response.body()?.string()

                if (data == null) {
                    data = response.errorBody()?.string()
                }
                val json = JSONObject(data!!)

                if (response.isSuccessful) {
                    val listaJson = json.getJSONArray("dados")
                    val listaCliente =
                        GsonBuilder().create().fromJson(listaJson.toString(), Array<Cliente>::class.java).toList()

                    // val st = "${listaJson.getJSONObject(1).getString("Codigo")} - ${listaJson.getJSONObject(1).getString("Razao")}"
                    //Toast.makeText(baseContext, "${st}", Toast.LENGTH_LONG).show()
                    id_listaCli.adapter = ListaAdapter(applicationContext, listaCliente)
                }

            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Toast.makeText(baseContext, "Errado!!!!", Toast.LENGTH_LONG).show()
            }
        })
    }

    fun ListarKotlin() {
        val retrofitClient = NetworkUtils.getRetrofitInstance(URL_PRINCIPAL)
        val endpoint = retrofitClient.create(Endpoint::class.java)
        lateinit var callback: Call<ResponseBody>

        if (DADOS_USUARIO.getInt("supVenda") == 1 || DADOS_USUARIO.getInt("genVenda") == 1) {
            callback = endpoint.getClientes()
        } else {
            callback = endpoint.getClientes(NOME_USUARIO)
        }

        callback.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                var data = response.body()?.string()

                if (data == null) {
                    data = response.errorBody()?.string()
                }
                //val json = JSONArray(data!!)

                if (response.isSuccessful) {
                    val dados = JSONObject(data!!).getJSONArray("dados")
                    listaCliente =
                        GsonBuilder().create().fromJson(dados.toString(), Array<Cliente>::class.java).toList()
                    listView.adapter = ListaAdapter(applicationContext, listaCliente)
                } else {
                    Toast.makeText(baseContext, "Não existe cliente", Toast.LENGTH_LONG).show()
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Toast.makeText(baseContext, "Errado!!!!", Toast.LENGTH_LONG).show()
            }
        })

    }

    fun pesquisaCodigo() {
        val retrofitClient = NetworkUtils.getRetrofitInstance(URL_PRINCIPAL)
        val endpoint = retrofitClient.create(Endpoint::class.java)
        val codigo = editCodigo.text.toString().toInt()
        val callback = endpoint.listClienteCodigo(codigo)

        callback.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                var data = response.body()?.string()

                if (data == null) {
                    data = response.errorBody()?.string()
                }
                val json = JSONObject(data!!)

                if (response.isSuccessful) {
                    listaCliente = listOf<Cliente>(
                        Cliente(
                            json.getString("codigo").toInt(), json.getString("fantasia")
                            /**,json.getString("cpfCnpj"),
                            json.getString("razao"), json.getDouble("limite"), json.getString("situacao") , json.getInt("bloqueado")**/
                            /**,json.getString("cpfCnpj"),
                            json.getString("razao"), json.getDouble("limite"), json.getString("situacao") , json.getInt("bloqueado")**/
                        )
                    )
                    id_listaCli.adapter = ListaAdapter(applicationContext, listaCliente)
                } else {
                    Toast.makeText(baseContext, "Não existe cliente com codigo", Toast.LENGTH_LONG).show()
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Toast.makeText(baseContext, "Errado!!!!", Toast.LENGTH_LONG).show()
            }
        })
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.menu_cliente, menu)

        return true

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle item selection
        return when (item.itemId) {
            R.id.menuPedidos -> {
                val intent = Intent(this, PedidosActivity::class.java)
                startActivity(intent)
                true
            }

            R.id.menuSair -> {
                finish()
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }
}