package com.example.bicypower.data.local.storage

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Copia un Uri (content://) a /files/images/IMG_yyyyMMdd_HHmmss.jpg
 * y retorna el content:// URI servido por FileProvider.
 */
fun copyImageToAppFiles(context: Context, source: Uri): Uri {
    val time = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
    val imagesDir = File(context.filesDir, "images").apply { if (!exists()) mkdirs() }
    val outFile = File(imagesDir, "IMG_$time.jpg")

    context.contentResolver.openInputStream(source).use { input: InputStream? ->
        FileOutputStream(outFile).use { output ->
            if (input != null) input.copyTo(output)
        }
    }

    return FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        outFile
    )
}
