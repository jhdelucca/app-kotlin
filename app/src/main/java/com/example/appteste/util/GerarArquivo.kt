package com.example.appteste.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Rect
import android.graphics.pdf.PdfDocument
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class GerarArquivo(var pasta:File , var context: Context) {

    private lateinit var arquivo:File

    fun salvarPDF(bitmap: Bitmap , nomeArquivo:String):String {
        arquivo = File(pasta, nomeArquivo+".pdf")

        var arquivoPdf = PdfDocument()
        val info = PdfDocument.PageInfo.Builder(bitmap.width,bitmap.height,1).create()
        val pagina = arquivoPdf.startPage(info)

        val canvas = pagina.canvas
        canvas.drawBitmap(bitmap,null, Rect(0,0,bitmap.width,bitmap.height),null)
        arquivoPdf.finishPage(pagina)

        try {
            arquivo.createNewFile()
            val streamDeSaida = FileOutputStream(arquivo)
            arquivoPdf.writeTo(streamDeSaida)
            streamDeSaida.close()
            arquivoPdf.close()
        } catch (e:IOException) {
            return "Erro ao criar arquivo " + e;
        }

        return "sucesso"
    }




}