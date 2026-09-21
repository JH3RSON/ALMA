package com.ima.alma.util

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.exifinterface.media.ExifInterface
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MediaManager(
    val requestCameraPermission: () -> Unit,
    val openCamera: () -> Unit,
    val openPhotoPicker: () -> Unit,
    val openMultiplePhotoPicker: () -> Unit
) {
    companion object {
        fun obtenerRutaOriginal(context: Context, nombreOPath: String): File {
            if (nombreOPath.isBlank()) return File("")
            val nombreArchivo = try {
                val path = Uri.parse(nombreOPath).path ?: nombreOPath
                File(path).name
            } catch (e: Exception) {
                File(nombreOPath).name
            }

            val dirOriginales = File(context.filesDir, "media/originales")
            val fOriginal = File(dirOriginales, nombreArchivo)
            if (fOriginal.exists()) return fOriginal

            // Fallback para archivos heredados en el directorio raíz de la app
            val fLegacy = File(context.filesDir, nombreArchivo)
            if (fLegacy.exists()) return fLegacy

            return File(nombreOPath)
        }

        fun obtenerRutaThumbnail(context: Context, nombreOPath: String): File {
            if (nombreOPath.isBlank()) return File("")
            val nombreArchivo = try {
                val path = Uri.parse(nombreOPath).path ?: nombreOPath
                File(path).name
            } catch (e: Exception) {
                File(nombreOPath).name
            }

            val dirThumbnails = File(context.filesDir, "media/thumbnails")
            val fThumbnail = File(dirThumbnails, nombreArchivo)
            if (fThumbnail.exists()) return fThumbnail

            // Fallback al archivo maestro original si la miniatura no existe
            return obtenerRutaOriginal(context, nombreOPath)
        }
    }
}

@Composable
fun rememberMediaManager(
    onCameraPermissionResult: (Boolean) -> Unit = {},
    onPhotoCaptured: (Uri?) -> Unit = {},
    onPhotoSelected: (Uri?) -> Unit = {},
    onMultiplePhotosSelected: (List<Uri>) -> Unit = {}
): MediaManager {
    val context = LocalContext.current
    var tempCameraUri by remember { mutableStateOf<Uri?>(null) }

    val cameraCaptureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && tempCameraUri != null) {
            val permanentUri = procesarYGuardarFotoEnStorage(context, tempCameraUri!!)
            onPhotoCaptured(permanentUri ?: tempCameraUri)
        } else {
            onPhotoCaptured(null)
        }
    }

    val ejecutarLanzamientoCamara = {
        try {
            val photoUri = crearArchivoTempImageUri(context)
            tempCameraUri = photoUri
            cameraCaptureLauncher.launch(photoUri)
        } catch (e: Exception) {
            e.printStackTrace()
            onPhotoCaptured(null)
        }
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        onCameraPermissionResult(isGranted)
        if (isGranted) {
            ejecutarLanzamientoCamara()
        }
    }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { pickerUri ->
        if (pickerUri != null) {
            val permanentUri = procesarYGuardarFotoEnStorage(context, pickerUri)
            onPhotoSelected(permanentUri ?: pickerUri)
        } else {
            onPhotoSelected(null)
        }
    }

    val multiplePhotoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(maxItems = 6)
    ) { uris ->
        if (uris.isNotEmpty()) {
            val copiedUris = uris.mapNotNull { uri ->
                procesarYGuardarFotoEnStorage(context, uri) ?: uri
            }
            onMultiplePhotosSelected(copiedUris)
        }
    }

    return remember(context) {
        MediaManager(
            requestCameraPermission = {
                cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
            },
            openCamera = {
                val hasPermission = ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.CAMERA
                ) == PackageManager.PERMISSION_GRANTED

                if (hasPermission) {
                    ejecutarLanzamientoCamara()
                } else {
                    cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                }
            },
            openPhotoPicker = {
                try {
                    photoPickerLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            },
            openMultiplePhotoPicker = {
                try {
                    multiplePhotoPickerLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        )
    }
}

fun obtenerRutaOriginal(context: Context, nombreOPath: String): File {
    return MediaManager.obtenerRutaOriginal(context, nombreOPath)
}

fun obtenerRutaThumbnail(context: Context, nombreOPath: String): File {
    return MediaManager.obtenerRutaThumbnail(context, nombreOPath)
}

fun copiarUriAAlmacenamientoInterno(context: Context, sourceUri: Uri): Uri? {
    return procesarYGuardarFotoEnStorage(context, sourceUri)
}

fun procesarYGuardarFotoEnStorage(context: Context, sourceUri: Uri): Uri? {
    return runBlocking(Dispatchers.IO) {
        try {
            val dirOriginales = File(context.filesDir, "media/originales").apply { mkdirs() }
            val dirThumbnails = File(context.filesDir, "media/thumbnails").apply { mkdirs() }

            val fileName = "ALMA_IMG_${System.currentTimeMillis()}_${(100..999).random()}.jpg"
            val masterFile = File(dirOriginales, fileName)

            // 1. Copia del Archivo Maestro (Calidad 100% Intacta)
            val inputStream = context.contentResolver.openInputStream(sourceUri) ?: return@runBlocking null
            FileOutputStream(masterFile).use { outputStream ->
                inputStream.use { input ->
                    input.copyTo(outputStream)
                }
            }

            // 2. Generador Automático de Miniatura (Thumbnail) en WebP 80% (Máx. 1080px ancho)
            val thumbnailFile = File(dirThumbnails, fileName)
            generarThumbnailEficiente(masterFile, thumbnailFile)

            Uri.fromFile(masterFile)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}

private suspend fun generarThumbnailEficiente(masterFile: File, thumbnailFile: File) = withContext(Dispatchers.IO) {
    try {
        val targetWidth = 1080

        // Lectura de dimensiones sin cargar el bitmap completo en RAM
        val optionsBounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(masterFile.absolutePath, optionsBounds)
        val origWidth = optionsBounds.outWidth

        var inSampleSize = 1
        if (origWidth > targetWidth) {
            var halfWidth = origWidth / 2
            while (halfWidth / inSampleSize >= targetWidth) {
                inSampleSize *= 2
            }
        }

        val decodeOptions = BitmapFactory.Options().apply {
            this.inSampleSize = inSampleSize
            inJustDecodeBounds = false
        }

        var bitmap = BitmapFactory.decodeFile(masterFile.absolutePath, decodeOptions) ?: return@withContext

        // Corregir la orientación EXIF de la imagen
        bitmap = corregirOrientacionExif(masterFile.absolutePath, bitmap)

        // Escalar con precisión al ancho objetivo manteniendo el aspecto original
        if (bitmap.width > targetWidth) {
            val scale = targetWidth.toFloat() / bitmap.width
            val targetHeight = (bitmap.height * scale).toInt()
            if (targetHeight > 0) {
                val scaledBitmap = Bitmap.createScaledBitmap(bitmap, targetWidth, targetHeight, true)
                if (scaledBitmap != bitmap) {
                    bitmap.recycle()
                    bitmap = scaledBitmap
                }
            }
        }

        // Guardar comprimido en WebP (calidad 80)
        FileOutputStream(thumbnailFile).use { fos ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                bitmap.compress(Bitmap.CompressFormat.WEBP_LOSSY, 80, fos)
            } else {
                @Suppress("DEPRECATION")
                bitmap.compress(Bitmap.CompressFormat.WEBP, 80, fos)
            }
        }
        bitmap.recycle()
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

private fun corregirOrientacionExif(path: String, bitmap: Bitmap): Bitmap {
    return try {
        val exif = ExifInterface(path)
        val orientation = exif.getAttributeInt(
            ExifInterface.TAG_ORIENTATION,
            ExifInterface.ORIENTATION_NORMAL
        )
        val degrees = when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> 90f
            ExifInterface.ORIENTATION_ROTATE_180 -> 180f
            ExifInterface.ORIENTATION_ROTATE_270 -> 270f
            else -> 0f
        }

        if (degrees != 0f) {
            val matrix = Matrix().apply { postRotate(degrees) }
            val rotated = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
            if (rotated != bitmap) {
                bitmap.recycle()
            }
            rotated
        } else {
            bitmap
        }
    } catch (e: Exception) {
        bitmap
    }
}

fun crearArchivoTempImageUri(context: Context): Uri {
    val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
    val imageFileName = "JPEG_${timeStamp}_"
    val storageDir = context.getExternalFilesDir("Pictures") ?: context.cacheDir
    val imageFile = File.createTempFile(imageFileName, ".jpg", storageDir)
    val authority = "${context.packageName}.fileprovider"
    return FileProvider.getUriForFile(context, authority, imageFile)
}
