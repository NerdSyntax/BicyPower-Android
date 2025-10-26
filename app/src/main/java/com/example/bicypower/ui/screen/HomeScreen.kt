package com.example.bicypower.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddShoppingCart
import androidx.compose.material.icons.filled.Person
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
import com.example.bicypower.data.CartStore
import com.example.bicypower.data.Product
import com.example.bicypower.data.local.product.ProductEntity
import com.example.bicypower.data.local.storage.UserPreferences
import com.example.bicypower.ui.viewmodel.HomeViewModel
import kotlinx.coroutines.launch
import android.net.Uri

@Composable
fun HomeScreen(
    onOpenProduct: (String) -> Unit = {},
    onAddToCart: (Product) -> Unit = {}
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val prefs = remember { UserPreferences(context) }

    // Foto persistida (solo para mostrar avatar, ya no hay botones de cámara en Home)
    val savedPhotoUri by prefs.photoUri.collectAsState(initial = null)
    var photoUriString by rememberSaveable { mutableStateOf<String?>(savedPhotoUri) }
    LaunchedEffect(savedPhotoUri) { photoUriString = savedPhotoUri }

    // Estado de sesión/rol para el icono
    val role by prefs.role.collectAsState(initial = "")
    val isLoggedIn by prefs.isLoggedIn.collectAsState(initial = false)

    val vm: HomeViewModel = viewModel()
    val state by vm.state.collectAsState()

    LaunchedEffect(state.errorMsg) {
        state.errorMsg?.let {
            // si hay error al agregar al carrito por stock, mostrar toast
            // (si prefieres Snackbar, cámbialo)
            // Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
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
        // ===== Header =====
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Ícono único:
                // - ADMIN => corona (emoji)
                // - USER/otro => persona; azul si logeado, gris si no
                if (role == "ADMIN") {
                    Text("👑", style = MaterialTheme.typography.headlineSmall)
                    Spacer(Modifier.width(8.dp))
                    Text("Admin", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold)
                } else {
                    val tint = if (isLoggedIn) Color(0xFF1E88E5) else MaterialTheme.colorScheme.outline
                    Icon(
                        imageVector = Icons.Filled.Person,
                        contentDescription = if (isLoggedIn) "Usuario logueado" else "Usuario no logueado",
                        tint = tint
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("BicyPower", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold)
                }
            }

            // Avatar (muestra foto guardada si existe; ya no hay acciones aquí)
            Box(
                Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                if (photoUriString.isNullOrBlank()) {
                    Icon(
                        imageVector = Icons.Filled.Person,
                        contentDescription = "avatar",
                        tint = MaterialTheme.colorScheme.primary
                    )
                } else {
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(Uri.parse(photoUriString))
                            .crossfade(true)
                            .build(),
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

        Spacer(Modifier.height(18.dp))

        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Productos", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            if (!state.isLoading) {
                Text(
                    "${filtered.size} ítems",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.Gray
                )
            }
        }

        Spacer(Modifier.height(10.dp))

        when {
            state.isLoading -> Box(
                Modifier
                    .fillMaxWidth()
                    .height(160.dp),
                contentAlignment = Alignment.Center
            ) { CircularProgressIndicator() }

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
                                        // validamos y restamos stock en VM; luego usamos tu CartStore
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
            Text(p.name, maxLines = 1, overflow = TextOverflow.Clip, fontWeight = FontWeight.SemiBold)
            Text("$ ${"%,.0f".format(p.price)}", color = MaterialTheme.colorScheme.primary)

            Spacer(Modifier.height(4.dp))
            if (p.stock <= 0) {
                AssistChip(onClick = {}, label = { Text("Agotado") })
            } else {
                AssistChip(onClick = {}, label = { Text("Stock: ${p.stock}") })
            }

            Spacer(Modifier.height(10.dp))
            Button(
                onClick = onAdd,
                enabled = p.stock > 0,
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
