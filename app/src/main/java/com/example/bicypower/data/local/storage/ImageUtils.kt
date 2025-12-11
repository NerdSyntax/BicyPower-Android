package com.example.bicypower.data.local.storage

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody

/** Crea un archivo temporal en /cache/images para la cámara. */
fun createTempImageFile(context: Context): File {
    val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
    val dir = File(context.cacheDir, "images").apply { if (!exists()) mkdirs() }
    return File(dir, "IMG_${timeStamp}.jpg")
}

/** Convierte un File a content:// Uri usando tu FileProvider. */
fun fileUri(context: Context, file: File): Uri {
    val authority = "${context.packageName}.fileprovider"
    return FileProvider.getUriForFile(context, authority, file)
}

/**
 * Convierte un content:// Uri de una imagen a MultipartBody.Part
 * para enviarla al microservicio de productos como archivo BLOB.
 */
fun uriToMultipart(context: Context, uri: Uri): MultipartBody.Part {
    val inputStream = context.contentResolver.openInputStream(uri)
        ?: throw IllegalStateException("No se pudo abrir la imagen seleccionada")

    val bytes = inputStream.readBytes()
    inputStream.close()

    val mediaType = "image/*".toMediaTypeOrNull()
    val requestBody = bytes.toRequestBody(mediaType)

    return MultipartBody.Part.createFormData(
        name = "archivo",         // debe ser el mismo nombre que en el controller Spring: @RequestParam("archivo")
        filename = "imagen.jpg",
        body = requestBody
    )
}
