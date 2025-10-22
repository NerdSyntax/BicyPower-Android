package com.example.bicypower.ui.screen

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Button
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.bicypower.data.Catalog
import com.example.bicypower.data.Product
import com.example.bicypower.data.local.storage.UserPreferences
import com.example.bicypower.data.local.storage.createTempImageFile
import com.example.bicypower.data.local.storage.fileUri
import kotlinx.coroutines.launch

@Composable
fun HomeScreen(
    onOpenProduct: (String) -> Unit = {},
    onAddToCart: (Product) -> Unit = {}
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val prefs = remember { UserPreferences(context) }

    // cargar/sincronizar foto desde DataStore
    val savedPhotoUri by prefs.photoUri.collectAsState(initial = null)
    var photoUriString by rememberSaveable { mutableStateOf<String?>(savedPhotoUri) }
    LaunchedEffect(savedPhotoUri) { photoUriString = savedPhotoUri }

    var pendingCapture: Uri? by remember { mutableStateOf<Uri?>(null) }
    val takePicture = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { ok ->
        if (ok) {
            photoUriString = pendingCapture?.toString()
            scope.launch { prefs.setPhoto(photoUriString) }
            Toast.makeText(context, "Foto guardada", Toast.LENGTH_SHORT).show()
        } else {
            pendingCapture = null
            Toast.makeText(context, "No se tomó foto", Toast.LENGTH_SHORT).show()
        }
    }

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("BicyPower", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold)
        Spacer(Modifier.height(12.dp))

        // Sección Foto
        ElevatedCard(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Tu foto", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(10.dp))

                if (photoUriString.isNullOrBlank()) {
                    Text("Aún no tomas una foto")
                } else {
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(Uri.parse(photoUriString))
                            .crossfade(true)
                            .build(),
                        contentDescription = "Foto",
                        modifier = Modifier.fillMaxWidth().height(160.dp),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(Modifier.height(10.dp))
                }

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(onClick = {
                        val f = createTempImageFile(context)
                        pendingCapture = fileUri(context, f)
                        takePicture.launch(pendingCapture)
                    }) {
                        Text(if (photoUriString.isNullOrBlank()) "Tomar foto" else "Volver a tomar")
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // Catálogo demo
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 160.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(Catalog.all, key = { it.id }) { p ->
                ProductCard(product = p, onClick = { onOpenProduct(p.id) }, onAdd = { onAddToCart(p) })
            }
        }
    }
}

@Composable
private fun ProductCard(
    product: Product,
    onClick: () -> Unit,
    onAdd: () -> Unit
) {
    ElevatedCard(Modifier.clickable(onClick = onClick)) {
        Column(Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(product.emoji, style = MaterialTheme.typography.displaySmall)
            Spacer(Modifier.height(6.dp))
            Text(product.name, fontWeight = FontWeight.SemiBold)
            Text("$ ${"%,.0f".format(product.price)}", color = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(6.dp))
            Button(onClick = onAdd, modifier = Modifier.fillMaxWidth()) { Text("Agregar") }
        }
    }
}
