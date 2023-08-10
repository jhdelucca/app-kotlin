package com.example.appteste.activity

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.ContentValues
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.example.appteste.R
import com.example.appteste.adapter.ListaPedidosAdapter
import com.example.appteste.adapter.ReportAdapter
import com.example.appteste.api.Endpoint
import com.example.appteste.database.ItensPedido
import com.example.appteste.database.PedidoUnico
import com.example.appteste.databinding.ActivityReportPedidoBinding
import com.example.appteste.extensions.FILIAL
import com.example.appteste.extensions.URL_PRINCIPAL
import com.example.appteste.request.PedidoUnicoRequest
import com.example.appteste.util.GerarArquivo
import com.example.appteste.util.NetworkUtils
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import kotlinx.android.synthetic.main.activity_report_pedido.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.*



class ReportPedidoActivity : AppCompatActivity()/**,ReportAdapter.ClickProduto**/ {

    private lateinit var binding : ActivityReportPedidoBinding
    var listaItens : List<ItensPedido> = listOf()
    lateinit var pedidoUnico: PedidoUnico

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityReportPedidoBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 1)
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), 1)

        val numero =  intent.getIntExtra("numero",0)
        val serie = intent.getStringExtra("serie")

        getPedidoUnico(numero, serie!!)



        binding.btnpdf.setOnClickListener {
            //screenshoot(window.decorView.rootView,"result")

            val bitmap = getScreenShotFromView(binding.cardView)
            val nomeArquivo = "ped2"

            // if bitmap is not null then
            // save it to gallery
            if (bitmap != null) {
               //saveMediaToStorage(bitmap)
                salvarPdf(bitmap,nomeArquivo)
            }
        }

       // val pedido = intent.extras!!.getParcelable<PedidosRequest>("pedido")
      //  getBuscaPedido(pedido)
    }

    fun getPedidoUnico(numero: Int , serie:String) {
        val retrofitClient = NetworkUtils.getRetrofitInstance(URL_PRINCIPAL)
        val endpoint = retrofitClient.create(Endpoint::class.java)

        val pedidos = PedidoUnicoRequest(FILIAL,serie,numero)

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
                        populaDados()


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
    @SuppressLint("SetTextI18n")
    private fun populaDados() {
        binding.recebeCliente.text = pedidoUnico.razao
        binding.recebeNumero.text = "${pedidoUnico.serie} - ${pedidoUnico.numero}"
        binding.recebeData.text = pedidoUnico.dtEmissao
        binding.recebePagamento.text = "${pedidoUnico.fpgDesc} - ${pedidoUnico.cpgDesc}"
        binding.recebeObs.text = pedidoUnico.pedObs
        binding.recebeVendedor.text = pedidoUnico.colaboradorRazao

        binding.idListaItens.adapter = ReportAdapter(applicationContext, listaItens)
    }

    private fun getScreenShotFromView(v: View): Bitmap? {
        // create a bitmap object
        var screenshot: Bitmap? = null
        try {
            // inflate screenshot object
            // with Bitmap.createBitmap it
            // requires three parameters
            // width and height of the view and
            // the background color
            screenshot = Bitmap.createBitmap(v.measuredWidth, v.measuredHeight, Bitmap.Config.ARGB_8888)
            // Now draw this bitmap on a canvas
            val canvas = Canvas(screenshot)
            v.draw(canvas)
        } catch (e: Exception) {
            Log.e("GFG", "Failed to capture screenshot because:" + e.message)
        }
        // return the bitmap
        return screenshot
    }


    // this method saves the image to gallery
    private fun saveMediaToStorage(bitmap: Bitmap) {
        // Generating a file name
        val filename = "${System.currentTimeMillis()}.jpg"

        // Output stream
        var fos: OutputStream? = null

        // For devices running android >= Q
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // getting the contentResolver
            this.contentResolver?.also { resolver ->

                // Content resolver will process the contentvalues
                val contentValues = ContentValues().apply {

                    // putting file information in content values
                    put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                    put(MediaStore.MediaColumns.MIME_TYPE, "image/jpg")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
                }

                // Inserting the contentValues to
                // contentResolver and getting the Uri
                val imageUri: Uri? = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)

                // Opening an outputstream with the Uri that we got
                fos = imageUri?.let { resolver.openOutputStream(it) }
            }
        } else {
            // These for devices running on android < Q
            val imagesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
            val image = File(imagesDir, filename)
            fos = FileOutputStream(image)
        }

        fos?.use {
            // Finally writing the bitmap to the output stream that we opened
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, it)
            Toast.makeText(this , "Captured View and saved to Gallery" , Toast.LENGTH_SHORT).show()
        }
    }

    private fun salvarPdf(bitmap: Bitmap , nomeArquivo:String) {
        val pasta = File(Environment.getExternalStorageDirectory() ,"/Download")
        val criarArquivo = GerarArquivo(pasta,applicationContext)

        val criando = criarArquivo.salvarPDF(bitmap,nomeArquivo)

        if(criando.equals("sucesso")) {
            Toast.makeText(applicationContext,"PDF criado" , Toast.LENGTH_SHORT).show()
        }else{
            Toast.makeText(applicationContext,"Erro PDF" , Toast.LENGTH_SHORT).show()
        }
    }


   /** private fun getBuscaPedido(pedido: PedidosRequest?) {
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

    }**/
    /**
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

    }**/

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.menu_pdf,menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle item selection
        return when (item.itemId) {
            R.id.menuPDF -> {
              //  val intent = Intent(this, ConfigActivity::class.java)
              //  startActivity(intent)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}