package com.example.bicypower.ui.screen

import android.app.Activity
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.bicypower.data.local.storage.UserPreferences
import com.example.bicypower.data.local.storage.createTempImageFile
import com.example.bicypower.data.local.storage.fileUri
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(
    onLogout: () -> Unit = {}
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val prefs = remember { UserPreferences(context) }

    val savedPhotoUri by prefs.photoUri.collectAsState(initial = null)
    var photoUriString by rememberSaveable { mutableStateOf<String?>(savedPhotoUri) }
    LaunchedEffect(savedPhotoUri) { photoUriString = savedPhotoUri }

    var pendingCapture: Uri? by remember { mutableStateOf<Uri?>(null) }
    val takePicture = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { ok ->
        if (ok) {
            photoUriString = pendingCapture?.toString()
            scope.launch { prefs.setPhoto(photoUriString) }
            Toast.makeText(context, "Foto actualizada", Toast.LENGTH_SHORT).show()
        } else {
            pendingCapture = null
        }
    }

    var darkMode by remember { mutableStateOf(false) }
    var notifications by remember { mutableStateOf(true) }

    Column(Modifier.padding(16.dp)) {
        Text("Settings", style = MaterialTheme.typography.titleLarge)

        Spacer(Modifier.height(12.dp))
        Text("Foto de perfil", style = MaterialTheme.typography.titleMedium)

        if (!photoUriString.isNullOrBlank()) {
            AsyncImage(
                model = ImageRequest.Builder(context).data(Uri.parse(photoUriString)).crossfade(true).build(),
                contentDescription = "Foto",
                modifier = Modifier.fillMaxWidth().height(160.dp),
                contentScale = ContentScale.Crop
            )
        } else {
            Text("Sin foto")
        }

        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(onClick = {
                val f = createTempImageFile(context)
                pendingCapture = fileUri(context, f)
                takePicture.launch(pendingCapture)
            }) { Text(if (photoUriString.isNullOrBlank()) "Tomar foto" else "Cambiar foto") }

            if (!photoUriString.isNullOrBlank()) {
                OutlinedButton(onClick = {
                    scope.launch { prefs.setPhoto(null) }
                    photoUriString = null
                    Toast.makeText(context, "Foto eliminada", Toast.LENGTH_SHORT).show()
                }) { Text("Eliminar foto") }
            }
        }

        Divider(Modifier.padding(vertical = 12.dp))

        ListItem(
            headlineContent = { Text("Modo oscuro") },
            trailingContent = { Switch(checked = darkMode, onCheckedChange = { darkMode = it }) }
        )
        ListItem(
            headlineContent = { Text("Notificaciones") },
            trailingContent = { Switch(checked = notifications, onCheckedChange = { notifications = it }) }
        )

        Spacer(Modifier.height(8.dp))
        Button(onClick = onLogout) { Text("Cerrar sesión") }
        Spacer(Modifier.height(8.dp))
        OutlinedButton(onClick = { (context as? Activity)?.finishAffinity() }) { Text("Salir de la app") }
    }
}
