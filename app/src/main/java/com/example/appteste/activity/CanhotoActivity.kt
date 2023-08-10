package com.example.appteste.activity

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.example.appteste.api.Endpoint
import com.example.appteste.databinding.ActivityCanhotoBinding
import com.example.appteste.model.Imagem
import com.example.appteste.util.NetworkUtils
import com.github.gcacace.signaturepad.views.SignaturePad
import com.google.gson.Gson
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream

class CanhotoActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCanhotoBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCanhotoBinding.inflate(layoutInflater)
        setContentView(binding.root)
        window.statusBarColor = Color.BLACK
        verifyStoragePermissions(this)

        binding.signaturePad.setOnSignedListener(object : SignaturePad.OnSignedListener {
            override fun onStartSigning() {

            }
            override fun onSigned() {
                binding.btnSalvar.isEnabled = true
                binding.btnLimpar.isEnabled = true
            }
            override fun onClear() {
                binding.btnSalvar.isEnabled = false
                binding.btnLimpar.isEnabled = false
            }
        })

        binding.btnLimpar.setOnClickListener {
           binding.signaturePad.clear()
        }
        binding.btnSalvar.setOnClickListener {
            if (addJpgSignatureToGallery(binding.signaturePad.signatureBitmap)) {
                Toast.makeText(
                    this,
                    "Assinatura salva",
                    Toast.LENGTH_SHORT
                ).show()
                binding.signaturePad.clear()
                finish()
            } else {
                Toast.makeText(
                    this,
                    "Não foi possivel salvar assinatura",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

    }

    private fun addJpgSignatureToGallery(signature: Bitmap): Boolean {
        var result = false
        try {
            val photo = File(
                getAlbumStorageDir("SignaturePad"),
                String.format("Signature_%d.jpg", System.currentTimeMillis())
            )
           // convertImageToByte(signature,photo)
           saveBitmapToJPG(signature, photo)
           // scanMediaFile(photo)
            result = true
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return result
    }

    fun getAlbumStorageDir(albumName: String?): File {
        // Get the directory for the user's public pictures directory.
        val file = File(
            Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES
            ), albumName.toString()
        )
        if (!file.mkdirs()) {
            Log.e("SignaturePad", "Direitorio não criado")
        }
        return file
    }

    @Throws(IOException::class)
    fun saveBitmapToJPG(bitmap: Bitmap, photo: File?) {
        val newBitmap = Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(newBitmap)
        canvas.drawColor(Color.WHITE)
        canvas.drawBitmap(bitmap, 0f, 0f, null)
        val stream: OutputStream = FileOutputStream(photo)
        newBitmap.compress(Bitmap.CompressFormat.JPEG, 80, stream)
        stream.close()

        if (photo != null) {
            Inserir(photo.readBytes())
            photo.delete()
        }

    }

    fun Inserir(byteArray: ByteArray) {
        val retrofitClient = NetworkUtils.getRetrofitInstance("http://192.168.0.100:8080/")
        val endpoint = retrofitClient.create(Endpoint::class.java)
        //val imgEspacao = img.replace("\n" ,"")
        val imagem = Imagem(1,byteArray)
        val body: RequestBody = Gson().toJson(imagem).toRequestBody("application/json".toMediaType())

        val callback = endpoint.postImagem(body)

        callback.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                Toast.makeText(baseContext, "Deu certo", Toast.LENGTH_LONG).show()
                /**   var data = response.body()?.string()

                if (data == null) {
                    data = response.errorBody()?.string()
                }
                val json = JSONObject(data!!)

                if(response.isSuccessful) {
                    Toast.makeText(baseContext, "Deu certo", Toast.LENGTH_LONG).show()

                }else{
                    Toast.makeText(baseContext, "${json.getString("error")}", Toast.LENGTH_LONG).show()
                } **/
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Toast.makeText(baseContext, "Errado!!!!", Toast.LENGTH_LONG).show()
            }
        })
    }

    private fun scanMediaFile(photo: File) {
        val mediaScanIntent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
        val contentUri = Uri.fromFile(photo)
        mediaScanIntent.data = contentUri
        this.sendBroadcast(mediaScanIntent)
    }

    companion object {
        private const val REQUEST_EXTERNAL_STORAGE = 1
        private val PERMISSIONS_STORAGE = arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        fun verifyStoragePermissions(activity: Activity?) {
            // Check if we have write permission
            val permission = ActivityCompat.checkSelfPermission(
                activity!!,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
            if (permission != PackageManager.PERMISSION_GRANTED) {
                // We don't have permission so prompt the user
                ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
                )
            }
        }
    }
}