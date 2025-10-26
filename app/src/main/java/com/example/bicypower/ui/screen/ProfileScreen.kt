package com.example.bicypower.ui.screen

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.bicypower.data.local.storage.UserPreferences
import com.example.bicypower.data.local.storage.createTempImageFile
import com.example.bicypower.data.local.storage.fileUri
import kotlinx.coroutines.launch

@Composable
fun ProfileScreen() {
    val context = LocalContext.current
    val prefs = remember { UserPreferences(context) }
    val scope = rememberCoroutineScope()

    val name by prefs.userName.collectAsState(initial = "")
    val email by prefs.userEmail.collectAsState(initial = "")
    val savedPhoto by prefs.photoUri.collectAsState(initial = null)

    var photoUri by remember { mutableStateOf<String?>(savedPhoto) }
    LaunchedEffect(savedPhoto) { photoUri = savedPhoto }

    var pendingCapture: Uri? by remember { mutableStateOf<Uri?>(null) }

    val takePicture = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { ok ->
        if (ok) {
            val s = pendingCapture?.toString()
            photoUri = s
            scope.launch { prefs.setPhoto(s) }
            Toast.makeText(context, "Foto actualizada", Toast.LENGTH_SHORT).show()
        } else {
            pendingCapture = null
        }
    }
    val pickFromGallery = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            val s = uri.toString()
            photoUri = s
            scope.launch { prefs.setPhoto(s) }
            Toast.makeText(context, "Foto seleccionada", Toast.LENGTH_SHORT).show()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(72.dp).clip(CircleShape),
                contentAlignment = Alignment.Center
            ) {
                if (photoUri.isNullOrBlank()) {
                    val initial = (name.trim().firstOrNull() ?: '?').uppercase()
                    Surface(shape = CircleShape, color = MaterialTheme.colorScheme.primaryContainer) {
                        Box(Modifier.size(72.dp), contentAlignment = Alignment.Center) {
                            Text(
                                text = initial.toString(),
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                } else {
                    AsyncImage(
                        model = ImageRequest.Builder(context).data(Uri.parse(photoUri)).crossfade(true).build(),
                        contentDescription = "Foto de perfil",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
            }
            Spacer(Modifier.width(12.dp))
            Column {
                Text(if (name.isNotBlank()) name else "Usuario",
                    style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
                if (email.isNotBlank()) {
                    Text(email, style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }

        Text("Foto de perfil", style = MaterialTheme.typography.titleMedium)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = {
                val f = createTempImageFile(context)
                pendingCapture = fileUri(context, f)
                takePicture.launch(pendingCapture)
            }) { Text(if (photoUri.isNullOrBlank()) "Tomar foto" else "Reemplazar con cámara") }

            OutlinedButton(onClick = { pickFromGallery.launch("image/*") }) { Text("Elegir de galería") }

            if (!photoUri.isNullOrBlank()) {
                OutlinedButton(onClick = {
                    photoUri = null
                    scope.launch { prefs.setPhoto(null) }
                    Toast.makeText(context, "Foto eliminada", Toast.LENGTH_SHORT).show()
                }) { Text("Eliminar") }
            }
        }

        ElevatedCard { ListItem(headlineContent = { Text("Mis pedidos") }) }
        ElevatedCard { ListItem(headlineContent = { Text("Direcciones") }) }
        ElevatedCard { ListItem(headlineContent = { Text("Métodos de pago") }) }
    }
}
