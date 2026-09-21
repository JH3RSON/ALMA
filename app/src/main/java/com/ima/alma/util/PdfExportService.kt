package com.ima.alma.util

import android.content.Context
import android.content.Intent
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Build
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import androidx.core.content.FileProvider
import androidx.core.content.res.ResourcesCompat
import androidx.exifinterface.media.ExifInterface
import coil.ImageLoader
import coil.request.ImageRequest
import com.ima.alma.R
import com.ima.alma.model.EntradaDiario
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

object PdfExportService {

    private const val PAGE_WIDTH = 595 // Formato A4 estándar (595 pt)
    private const val PAGE_HEIGHT = 842 // Formato A4 estándar (842 pt)
    private const val MARGIN_X = 48f // Margen general de 48 pt
    private const val MARGIN_Y = 48f
    private const val CONTENT_WIDTH = PAGE_WIDTH - (MARGIN_X * 2) // 499 pt
    private const val MAX_PRINT_Y = 750f // Límite inferior seguro

    suspend fun exportarYCompartirPdf(
        context: Context,
        entradas: List<EntradaDiario>,
        tituloDocumento: String = "Diario_ALMA"
    ): Uri? = withContext(Dispatchers.IO) {
        val pdfDocument = PdfDocument()

        // Inyección de fuentes iOS sf_pro_rounded desde recursos
        val typefaceRegular = ResourcesCompat.getFont(context, R.font.sf_pro_rounded_regular) ?: Typeface.DEFAULT
        val typefaceBold = ResourcesCompat.getFont(context, R.font.sf_pro_rounded_bold) ?: Typeface.DEFAULT_BOLD

        val bgPaint = Paint().apply { color = Color.parseColor("#FAF7F2") }

        val headerDatePaint = TextPaint().apply {
            color = Color.parseColor("#2C221E")
            textSize = 14f
            typeface = typefaceBold
            isAntiAlias = true
        }

        val dividerPaint = Paint().apply {
            color = Color.parseColor("#D7CCC8")
            style = Paint.Style.STROKE
            strokeWidth = 0.5f
        }

        val textPaint = TextPaint().apply {
            color = Color.parseColor("#1C1917")
            textSize = 12f
            typeface = typefaceRegular
            isAntiAlias = true
        }

        val hashtagPaint = TextPaint().apply {
            color = Color.parseColor("#007AFF")
            textSize = 11f
            typeface = typefaceRegular
            isAntiAlias = true
        }

        val footerPaint = TextPaint().apply {
            color = Color.parseColor("#8E8E93")
            textSize = 10f
            typeface = typefaceRegular
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }

        var pageNumber = 1
        var currentPageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create()
        var currentPage = pdfDocument.startPage(currentPageInfo)
        var canvas = currentPage.canvas

        fun dibujarFondo(c: Canvas) {
            c.drawRect(0f, 0f, PAGE_WIDTH.toFloat(), PAGE_HEIGHT.toFloat(), bgPaint)
        }

        fun finalizarPaginaActual() {
            canvas.drawText("— Página $pageNumber —", (PAGE_WIDTH / 2).toFloat(), 805f, footerPaint)
            pdfDocument.finishPage(currentPage)
        }

        dibujarFondo(canvas)
        var currentY = MARGIN_Y

        val sdfHeader = SimpleDateFormat("EEEE, d 'de' MMMM, yyyy", Locale.getDefault())

        for (entrada in entradas) {
            val fechaText = sdfHeader.format(Date(entrada.fechaMilisegundos)).uppercase()
            val fotos = entrada.obtenerTodasLasFotos()

            // Cargar bitmaps ligeros (redimensionados a máximo 500 px)
            val bitmaps = fotos.mapNotNull { uri -> cargarBitmapLiguero(context, uri) }

            // Calcular StaticLayout para el texto
            val staticLayoutTexto = crearStaticLayout(entrada.texto, textPaint, CONTENT_WIDTH.toInt())

            var alturaFotos = 0f
            if (bitmaps.size == 1) {
                val b = bitmaps[0]
                val scale = CONTENT_WIDTH / b.width
                alturaFotos = b.height * scale + 12f
            } else if (bitmaps.size >= 2) {
                val fotoAncho = (CONTENT_WIDTH - 12f) / 2f
                val b0 = bitmaps[0]
                val b1 = bitmaps[1]
                val h0 = b0.height * (fotoAncho / b0.width)
                val h1 = b1.height * (fotoAncho / b1.width)
                alturaFotos = Math.min(Math.max(h0, h1), 220f) + 12f
            }

            // Comprobar si la cabecera + texto + fotos sobrepasan el margen seguro
            if (currentY + 40f + staticLayoutTexto.height + alturaFotos > MAX_PRINT_Y && currentY > MARGIN_Y + 40f) {
                finalizarPaginaActual()
                pageNumber++
                currentPageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create()
                currentPage = pdfDocument.startPage(currentPageInfo)
                canvas = currentPage.canvas
                dibujarFondo(canvas)
                currentY = MARGIN_Y
            }

            // Cabecera: Fecha en negrita redondeada y línea divisoria imperceptible de 0.5 pt
            canvas.drawText(fechaText, MARGIN_X, currentY, headerDatePaint)
            currentY += 16f
            canvas.drawLine(MARGIN_X, currentY, MARGIN_X + CONTENT_WIDTH, currentY, dividerPaint)
            currentY += 16f

            // Texto renderizado con StaticLayout usando sf_pro_rounded_regular
            if (entrada.texto.isNotBlank()) {
                if (currentY + staticLayoutTexto.height > MAX_PRINT_Y) {
                    finalizarPaginaActual()
                    pageNumber++
                    currentPageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create()
                    currentPage = pdfDocument.startPage(currentPageInfo)
                    canvas = currentPage.canvas
                    dibujarFondo(canvas)
                    currentY = MARGIN_Y
                }

                canvas.save()
                canvas.translate(MARGIN_X, currentY)
                staticLayoutTexto.draw(canvas)
                canvas.restore()
                currentY += staticLayoutTexto.height + 12f
            }

            // Fotos ligeras
            if (bitmaps.isNotEmpty()) {
                if (bitmaps.size == 1) {
                    val b = bitmaps[0]
                    val scale = CONTENT_WIDTH / b.width
                    val scaledH = b.height * scale
                    if (currentY + scaledH > MAX_PRINT_Y) {
                        finalizarPaginaActual()
                        pageNumber++
                        currentPageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create()
                        currentPage = pdfDocument.startPage(currentPageInfo)
                        canvas = currentPage.canvas
                        dibujarFondo(canvas)
                        currentY = MARGIN_Y
                    }
                    val destRect = RectF(MARGIN_X, currentY, MARGIN_X + CONTENT_WIDTH, currentY + scaledH)
                    canvas.drawBitmap(b, null, destRect, null)
                    currentY += scaledH + 12f
                } else {
                    val fotoAncho = (CONTENT_WIDTH - 12f) / 2f
                    val b0 = bitmaps[0]
                    val b1 = bitmaps[1]
                    val h0 = b0.height * (fotoAncho / b0.width)
                    val h1 = b1.height * (fotoAncho / b1.width)
                    val maxH = Math.min(Math.max(h0, h1), 220f)

                    if (currentY + maxH > MAX_PRINT_Y) {
                        finalizarPaginaActual()
                        pageNumber++
                        currentPageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create()
                        currentPage = pdfDocument.startPage(currentPageInfo)
                        canvas = currentPage.canvas
                        dibujarFondo(canvas)
                        currentY = MARGIN_Y
                    }

                    val dest0 = RectF(MARGIN_X, currentY, MARGIN_X + fotoAncho, currentY + maxH)
                    val dest1 = RectF(MARGIN_X + fotoAncho + 12f, currentY, MARGIN_X + CONTENT_WIDTH, currentY + maxH)
                    canvas.drawBitmap(b0, null, dest0, null)
                    canvas.drawBitmap(b1, null, dest1, null)
                    currentY += maxH + 12f
                }
            }

            // Hashtags
            if (!entrada.hashtags.isNullOrBlank()) {
                val staticLayoutHashtag = crearStaticLayout(entrada.hashtags, hashtagPaint, CONTENT_WIDTH.toInt())
                if (currentY + staticLayoutHashtag.height > MAX_PRINT_Y) {
                    finalizarPaginaActual()
                    pageNumber++
                    currentPageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create()
                    currentPage = pdfDocument.startPage(currentPageInfo)
                    canvas = currentPage.canvas
                    dibujarFondo(canvas)
                    currentY = MARGIN_Y
                }
                canvas.save()
                canvas.translate(MARGIN_X, currentY)
                staticLayoutHashtag.draw(canvas)
                canvas.restore()
                currentY += staticLayoutHashtag.height + 12f
            }

            currentY += 20f
        }

        finalizarPaginaActual()

        val pdfFile = File(context.cacheDir, "$tituloDocumento.pdf")
        FileOutputStream(pdfFile).use { out ->
            pdfDocument.writeTo(out)
        }
        pdfDocument.close()

        val authority = "${context.packageName}.fileprovider"
        FileProvider.getUriForFile(context, authority, pdfFile)
    }

    fun compartirPdfUri(context: Context, pdfUri: Uri) {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, pdfUri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(shareIntent, "Exportar Diario a PDF"))
    }

    private fun crearStaticLayout(texto: String, textPaint: TextPaint, width: Int): StaticLayout {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            StaticLayout.Builder.obtain(texto, 0, texto.length, textPaint, width)
                .setAlignment(Layout.Alignment.ALIGN_NORMAL)
                .setLineSpacing(0f, 1.15f)
                .setIncludePad(false)
                .build()
        } else {
            @Suppress("DEPRECATION")
            StaticLayout(texto, textPaint, width, Layout.Alignment.ALIGN_NORMAL, 1.15f, 0f, false)
        }
    }

    private suspend fun cargarBitmapLiguero(context: Context, uriString: String): Bitmap? = withContext(Dispatchers.IO) {
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

            // Redimensionar a máximo 500 px para evitar consumo excesivo de RAM en Canvas
            val maxAncho = 500
            val bitmapLiguero = if (originalBitmap.width > maxAncho) {
                val scale = maxAncho.toFloat() / originalBitmap.width
                val nuevoAlto = (originalBitmap.height * scale).toInt()
                Bitmap.createScaledBitmap(originalBitmap, maxAncho, nuevoAlto, true)
            } else {
                originalBitmap
            }

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
                Bitmap.createBitmap(bitmapLiguero, 0, 0, bitmapLiguero.width, bitmapLiguero.height, matrix, true)
            } else {
                bitmapLiguero
            }
        } catch (e: Exception) {
            null
        }
    }
}
