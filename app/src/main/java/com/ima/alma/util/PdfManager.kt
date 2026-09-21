package com.ima.alma.util

import android.content.Context
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.graphics.pdf.PdfDocument
import android.net.Uri
import androidx.exifinterface.media.ExifInterface
import coil.ImageLoader
import coil.request.ImageRequest
import com.ima.alma.model.EntradaDiario
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

object PdfManager {

    suspend fun exportarDiarioAPdf(
        context: Context,
        uriDestino: Uri,
        entradas: List<EntradaDiario>,
        fotoPortadaUri: String?
    ) = withContext(Dispatchers.IO) {
        val pdfDocument = PdfDocument()

        val pageWidth = 595
        val pageHeight = 842

        val bgPaint = Paint().apply { color = Color.parseColor("#FAF7F2") }
        val titlePaint = Paint().apply {
            color = Color.parseColor("#2C221E")
            textSize = 28f
            typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }
        val subtitlePaint = Paint().apply {
            color = Color.parseColor("#6B5E57")
            textSize = 14f
            typeface = Typeface.create(Typeface.SERIF, Typeface.ITALIC)
            textAlign = Paint.Align.CENTER
        }
        val headerPaint = Paint().apply {
            color = Color.parseColor("#3E2723")
            textSize = 14f
            typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
        }
        val textPaint = Paint().apply {
            color = Color.parseColor("#1C1917")
            textSize = 12f
            typeface = Typeface.create(Typeface.SERIF, Typeface.NORMAL)
        }
        val borderPaint = Paint().apply {
            color = Color.parseColor("#D7CCC8")
            style = Paint.Style.STROKE
            strokeWidth = 2f
        }

        // PÁGINA 1: PORTADA
        val pageInfo1 = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create()
        val page1 = pdfDocument.startPage(pageInfo1)
        val canvas1 = page1.canvas

        canvas1.drawRect(0f, 0f, pageWidth.toFloat(), pageHeight.toFloat(), bgPaint)
        canvas1.drawRect(20f, 20f, (pageWidth - 20).toFloat(), (pageHeight - 20).toFloat(), borderPaint)
        canvas1.drawRect(24f, 24f, (pageWidth - 24).toFloat(), (pageHeight - 24).toFloat(), borderPaint)

        if (!fotoPortadaUri.isNullOrBlank()) {
            val bitmapPortada = cargarBitmapConCoilYOrientacion(context, fotoPortadaUri)
            if (bitmapPortada != null) {
                val maxImgWidth = 320f
                val maxImgHeight = 320f
                val scale = Math.min(maxImgWidth / bitmapPortada.width, maxImgHeight / bitmapPortada.height)
                val scaledWidth = bitmapPortada.width * scale
                val scaledHeight = bitmapPortada.height * scale
                val left = (pageWidth - scaledWidth) / 2f
                val top = 180f

                val destRect = RectF(left, top, left + scaledWidth, top + scaledHeight)
                canvas1.drawBitmap(bitmapPortada, null, destRect, null)
            }
        }

        canvas1.drawText("ALMA", (pageWidth / 2).toFloat(), 580f, titlePaint)
        canvas1.drawText("Mi Diario Personal y Memorias", (pageWidth / 2).toFloat(), 615f, subtitlePaint)
        canvas1.drawText("«El viaje de mil millas comienza con un solo paso»", (pageWidth / 2).toFloat(), 660f, subtitlePaint)
        canvas1.drawText("BY IMA", (pageWidth / 2).toFloat(), 740f, subtitlePaint)

        pdfDocument.finishPage(page1)

        // PÁGINAS SIGUIENTES: ENTRADAS
        var pageNumber = 2
        var currentPageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
        var currentPage = pdfDocument.startPage(currentPageInfo)
        var canvas = currentPage.canvas

        fun prepararCanvasPagina(c: Canvas) {
            c.drawRect(0f, 0f, pageWidth.toFloat(), pageHeight.toFloat(), bgPaint)
            c.drawRect(25f, 25f, (pageWidth - 25).toFloat(), (pageHeight - 25).toFloat(), borderPaint)
        }

        prepararCanvasPagina(canvas)
        var currentY = 50f
        val marginX = 40f
        val contentWidth = pageWidth - (marginX * 2)

        val sdf = SimpleDateFormat("dd 'de' MMMM, yyyy - HH:mm", Locale.getDefault())

        for (entrada in entradas) {
            val fechaText = sdf.format(Date(entrada.fechaMilisegundos)).uppercase()
            val hashtagsText = entrada.hashtags ?: ""

            var requiredHeight = 40f
            val lineasTexto = dividirTextoEnLineas(entrada.texto, textPaint, contentWidth)
            requiredHeight += lineasTexto.size * 16f

            if (hashtagsText.isNotBlank()) requiredHeight += 20f

            val bitmapEntrada = if (!entrada.fotoUri.isNullOrBlank()) cargarBitmapConCoilYOrientacion(context, entrada.fotoUri) else null
            if (bitmapEntrada != null) requiredHeight += 200f

            if (currentY + requiredHeight > pageHeight - 60f) {
                pdfDocument.finishPage(currentPage)
                pageNumber++
                currentPageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
                currentPage = pdfDocument.startPage(currentPageInfo)
                canvas = currentPage.canvas
                prepararCanvasPagina(canvas)
                currentY = 50f
            }

            canvas.drawText(fechaText, marginX, currentY, headerPaint)
            currentY += 20f

            if (bitmapEntrada != null) {
                val imgHeight = 180f
                val scale = Math.min(contentWidth / bitmapEntrada.width, imgHeight / bitmapEntrada.height)
                val scaledW = bitmapEntrada.width * scale
                val scaledH = bitmapEntrada.height * scale
                val destRect = RectF(marginX, currentY, marginX + scaledW, currentY + scaledH)
                canvas.drawBitmap(bitmapEntrada, null, destRect, null)
                currentY += scaledH + 12f
            }

            for (linea in lineasTexto) {
                canvas.drawText(linea, marginX, currentY, textPaint)
                currentY += 16f
            }

            if (hashtagsText.isNotBlank()) {
                currentY += 4f
                val hashtagPaint = Paint(subtitlePaint).apply {
                    textAlign = Paint.Align.LEFT
                    textSize = 11f
                    color = Color.parseColor("#007AFF")
                }
                canvas.drawText(hashtagsText, marginX, currentY, hashtagPaint)
                currentY += 16f
            }

            currentY += 12f
            canvas.drawLine(marginX, currentY, marginX + contentWidth, currentY, borderPaint)
            currentY += 20f
        }

        if (currentY + 60f > pageHeight - 60f) {
            pdfDocument.finishPage(currentPage)
            pageNumber++
            currentPageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
            currentPage = pdfDocument.startPage(currentPageInfo)
            canvas = currentPage.canvas
            prepararCanvasPagina(canvas)
            currentY = 50f
        }

        currentY += 30f
        canvas.drawText("FIN", (pageWidth / 2).toFloat(), currentY, subtitlePaint)
        currentY += 24f
        canvas.drawText("GRACIAS POR HABLAR CON TU ALMA", (pageWidth / 2).toFloat(), currentY, subtitlePaint)

        pdfDocument.finishPage(currentPage)

        try {
            context.contentResolver.openOutputStream(uriDestino)?.use { outputStream ->
                pdfDocument.writeTo(outputStream)
            }
        } finally {
            pdfDocument.close()
        }
    }

    private suspend fun cargarBitmapConCoilYOrientacion(context: Context, uriString: String): Bitmap? {
        return withContext(Dispatchers.IO) {
            try {
                val uri = Uri.parse(uriString)
                val imageLoader = ImageLoader(context)
                val request = ImageRequest.Builder(context)
                    .data(uri)
                    .allowHardware(false)
                    .build()

                val result = imageLoader.execute(request)
                val drawable = result.drawable ?: return@withContext null
                val originalBitmap = (drawable as? BitmapDrawable)?.bitmap ?: return@withContext null

                var degrees = 0f
                try {
                    val inputStreamForExif = context.contentResolver.openInputStream(uri)
                    if (inputStreamForExif != null) {
                        val exif = ExifInterface(inputStreamForExif)
                        val orientation = exif.getAttributeInt(
                            ExifInterface.TAG_ORIENTATION,
                            ExifInterface.ORIENTATION_NORMAL
                        )
                        inputStreamForExif.close()
                        degrees = when (orientation) {
                            ExifInterface.ORIENTATION_ROTATE_90 -> 90f
                            ExifInterface.ORIENTATION_ROTATE_180 -> 180f
                            ExifInterface.ORIENTATION_ROTATE_270 -> 270f
                            else -> 0f
                        }
                    }
                } catch (_: Exception) { }

                if (degrees != 0f) {
                    val matrix = Matrix().apply { postRotate(degrees) }
                    Bitmap.createBitmap(
                        originalBitmap,
                        0,
                        0,
                        originalBitmap.width,
                        originalBitmap.height,
                        matrix,
                        true
                    )
                } else {
                    originalBitmap
                }
            } catch (e: Exception) {
                null
            }
        }
    }

    private fun dividirTextoEnLineas(texto: String, paint: Paint, maxWidth: Float): List<String> {
        val result = mutableListOf<String>()
        val lineasParrafo = texto.split("\n")
        for (parrafo in lineasParrafo) {
            val palabras = parrafo.split(" ")
            var lineaActual = ""
            for (palabra in palabras) {
                val intento = if (lineaActual.isEmpty()) palabra else "$lineaActual $palabra"
                if (paint.measureText(intento) <= maxWidth) {
                    lineaActual = intento
                } else {
                    if (lineaActual.isNotEmpty()) result.add(lineaActual)
                    lineaActual = palabra
                }
            }
            if (lineaActual.isNotEmpty()) result.add(lineaActual)
        }
        return result
    }
}
