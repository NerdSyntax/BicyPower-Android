package com.example.bicypower.ui.screen

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
fun ProfileScreen(
    onOpenOrders: () -> Unit,
    onOpenAddresses: () -> Unit,
    onOpenPayments: () -> Unit
) {
    val context = LocalContext.current
    val prefs = remember { UserPreferences(context) }
    val scope = rememberCoroutineScope()

    val name  by prefs.userName.collectAsState(initial = "Cliente")
    val email by prefs.userEmail.collectAsState(initial = "")
    val savedPhoto by prefs.photoUri.collectAsState(initial = null)

    var photoUri by remember { mutableStateOf<String?>(savedPhoto) }
    LaunchedEffect(savedPhoto) { photoUri = savedPhoto }

    // --- launchers ---
    var pendingCapture: Uri? by remember { mutableStateOf<Uri?>(null) }

    val takePicture = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { ok ->
        if (ok) {
            val s = pendingCapture?.toString()
            photoUri = s
            scope.launch { prefs.setPhoto(s) }
            Toast.makeText(context, "Foto actualizada", Toast.LENGTH_SHORT).show()
        } else {
            pendingCapture = null
        }
    }

    val pickFromGallery = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            val s = uri.toString()
            photoUri = s
            scope.launch { prefs.setPhoto(s) }
            Toast.makeText(context, "Foto seleccionada", Toast.LENGTH_SHORT).show()
        }
    }

    Column(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Header con avatar + datos
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(56.dp).clip(CircleShape),
                contentAlignment = Alignment.Center
            ) {
                if (photoUri.isNullOrBlank()) {
                    Surface(shape = CircleShape, color = MaterialTheme.colorScheme.primaryContainer) {
                        Box(Modifier.size(56.dp), contentAlignment = Alignment.Center) {
                            Text((name.firstOrNull() ?: 'U').uppercaseChar().toString())
                        }
                    }
                } else {
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(Uri.parse(photoUri))
                            .crossfade(true).build(),
                        contentDescription = "Avatar",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
            }
            Spacer(Modifier.width(12.dp))
            Column {
                Text(name, style = MaterialTheme.typography.titleMedium)
                if (email.isNotBlank()) {
                    Text(email, style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }

        Text("Foto de perfil", style = MaterialTheme.typography.titleMedium)

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                onClick = {
                    val f = createTempImageFile(context)
                    pendingCapture = fileUri(context, f)
                    takePicture.launch(pendingCapture)
                }
            ) { Text(if (photoUri.isNullOrBlank()) "Tomar foto" else "Reemplazar con cámara") }

            OutlinedButton(onClick = { pickFromGallery.launch("image/*") }) {
                Text("Elegir de galería")
            }
        }

        // Accesos
        ElevatedCard(Modifier.fillMaxWidth().clickable { onOpenOrders() }) {
            ListItem(headlineContent = { Text("Mis pedidos") })
        }
        ElevatedCard(Modifier.fillMaxWidth().clickable { onOpenAddresses() }) {
            ListItem(headlineContent = { Text("Direcciones") })
        }
        ElevatedCard(Modifier.fillMaxWidth().clickable { onOpenPayments() }) {
            ListItem(headlineContent = { Text("Métodos de pago") })
        }
    }
}
