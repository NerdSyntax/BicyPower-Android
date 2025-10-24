package com.example.bicypower.ui.screen

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddShoppingCart
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.bicypower.data.CartStore // ← deja tu import tal y como lo tenías
import com.example.bicypower.data.local.product.ProductEntity
import com.example.bicypower.data.local.storage.UserPreferences
import com.example.bicypower.data.local.storage.createTempImageFile
import com.example.bicypower.data.local.storage.fileUri
import com.example.bicypower.ui.viewmodel.HomeViewModel
import kotlinx.coroutines.launch

@Composable
fun HomeScreen(
    onOpenProduct: (String) -> Unit = {},
    onAddToCart: (com.example.bicypower.data.Product) -> Unit = {} // se mantiene, aunque no lo usemos aquí
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
            Toast.makeText(context, "Foto tomada", Toast.LENGTH_SHORT).show()
        } else {
            pendingCapture = null
        }
    }
    val pickFromGallery = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            photoUriString = uri.toString()
            scope.launch { prefs.setPhoto(photoUriString) }
            Toast.makeText(context, "Foto seleccionada", Toast.LENGTH_SHORT).show()
        }
    }

    val vm: HomeViewModel = viewModel()
    val state by vm.state.collectAsState()

    // 👇 CAMBIO: mostrar mensaje del VM
    LaunchedEffect(state.errorMsg) {
        state.errorMsg?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            vm.clearError()
        }
    }

    var query by remember { mutableStateOf("") }
    val filtered = remember(state.items, query) {
        if (query.isBlank()) state.items
        else state.items.filter { it.name.contains(query, ignoreCase = true) }
    }

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        // HEADER (igual que ya lo tenías)
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Hola 👋", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                Text("BicyPower", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold)
            }
            Box(
                Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                if (photoUriString.isNullOrBlank()) {
                    Icon(Icons.Filled.CameraAlt, contentDescription = "avatar", tint = MaterialTheme.colorScheme.primary)
                } else {
                    AsyncImage(
                        model = ImageRequest.Builder(context).data(Uri.parse(photoUriString)).crossfade(true).build(),
                        contentDescription = "avatar",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
            placeholder = { Text("Buscar bicicletas, cascos, luces...") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(14.dp)
        )

        Spacer(Modifier.height(12.dp))

        ElevatedCard(
            Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(Modifier.weight(1f)) {
                    Text("Envío gratis", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text("Celebra el pedaleo 🚴", style = MaterialTheme.typography.bodyMedium)
                    Spacer(Modifier.height(8.dp))
                    Button(onClick = { /* promo */ }, shape = RoundedCornerShape(12.dp)) { Text("Ver ofertas") }
                }
                Spacer(Modifier.width(12.dp))
            }
        }

        Spacer(Modifier.height(14.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ElevatedButton(
                onClick = {
                    val f = createTempImageFile(context)
                    pendingCapture = fileUri(context, f)
                    takePicture.launch(pendingCapture)
                },
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Filled.CameraAlt, contentDescription = null)
                Spacer(Modifier.width(6.dp))
                Text("Tomar foto")
            }
            OutlinedButton(
                onClick = { pickFromGallery.launch("image/*") },
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Filled.Image, contentDescription = null)
                Spacer(Modifier.width(6.dp))
                Text("Galería")
            }
        }

        Spacer(Modifier.height(18.dp))

        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Productos", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            if (!state.isLoading) Text("${filtered.size} ítems", style = MaterialTheme.typography.labelMedium, color = Color.Gray)
        }

        Spacer(Modifier.height(10.dp))

        when {
            state.isLoading -> Box(Modifier.fillMaxWidth().height(160.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            filtered.isEmpty() -> Text("Aún no hay productos creados. Añádelos desde Admin.", color = Color.Gray)
            else -> {
                val rows = remember(filtered) { filtered.chunked(2) }
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    rows.forEach { row ->
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            row.forEach { p ->
                                ProductCardDb(
                                    p = p,
                                    onAdd = {
                                        // 👇 CAMBIO: valida stock con VM y SI OK, agrega con tu CartStore actual
                                        vm.addToCart(p) { CartStore.addDb(p) }
                                    },
                                    onOpen = { onOpenProduct(p.id.toString()) },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            if (row.size == 1) Spacer(Modifier.weight(1f))
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(18.dp))
    }
}

@Composable
private fun ProductCardDb(
    p: ProductEntity,
    onAdd: () -> Unit,
    onOpen: () -> Unit,
    modifier: Modifier = Modifier
) {
    ElevatedCard(
        modifier = modifier.clickable(onClick = onOpen),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(Modifier.padding(12.dp)) {
            if (p.imageUrl.isNotBlank()) {
                AsyncImage(
                    model = p.imageUrl,
                    contentDescription = p.name,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Crop
                )
                Spacer(Modifier.height(8.dp))
            }
            Text(p.name, maxLines = 1, overflow = TextOverflow.Ellipsis, fontWeight = FontWeight.SemiBold)
            Text("$ ${"%,.0f".format(p.price)}", color = MaterialTheme.colorScheme.primary)

            // 👇 CAMBIO: mostrar stock/agotado
            Spacer(Modifier.height(4.dp))
            if (p.stock <= 0) {
                AssistChip(onClick = {}, label = { Text("Agotado") })
            } else {
                AssistChip(onClick = {}, label = { Text("Stock: ${p.stock}") })
            }

            Spacer(Modifier.height(10.dp))
            Button(
                onClick = onAdd,
                enabled = p.stock > 0, // 👈 CAMBIO: deshabilitar sin stock
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Filled.AddShoppingCart, contentDescription = null)
                Spacer(Modifier.width(6.dp))
                Text(if (p.stock > 0) "Agregar" else "Agotado")
            }
        }
    }
}
