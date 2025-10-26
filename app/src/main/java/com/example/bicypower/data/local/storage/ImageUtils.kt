package com.example.bicypower.data.local.storage

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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
