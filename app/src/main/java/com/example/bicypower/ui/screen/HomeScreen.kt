package com.example.bicypower.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddShoppingCart
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.example.bicypower.data.local.session.UserSession
import com.example.bicypower.data.remote.dto.ProductDtoRemote
import com.example.bicypower.ui.viewmodel.HomeViewModel

@Composable
fun HomeScreen(
    onOpenProduct: (String) -> Unit = {},
    onAddToCart: (ProductDtoRemote) -> Unit = {}
) {
    val context = LocalContext.current
    val session = remember { UserSession(context) }

    val role by session.role.collectAsState(initial = "")
    val isLoggedIn by session.isLoggedIn.collectAsState(initial = false)

    val vm: HomeViewModel = viewModel()
    val state by vm.state.collectAsState()

    var query by remember { mutableStateOf("") }

    val filtered = remember(state.items, query) {
        if (query.isBlank()) state.items
        else state.items.filter { (it.nombre ?: "").contains(query, ignoreCase = true) }
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
                if (role == "ADMIN") {
                    Text("👑", style = MaterialTheme.typography.headlineSmall)
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "Admin",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold
                    )
                } else {
                    val tint = if (isLoggedIn) Color(0xFF1E88E5) else MaterialTheme.colorScheme.outline
                    Icon(
                        imageVector = Icons.Filled.Person,
                        contentDescription = if (isLoggedIn) "Usuario logueado" else "Usuario no logueado",
                        tint = tint
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "BicyPower",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }

            Box(
                Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Person,
                    contentDescription = "avatar",
                    tint = MaterialTheme.colorScheme.primary
                )
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

            filtered.isEmpty() -> Text("No hay productos disponibles.", color = Color.Gray)

            else -> {
                val rows = remember(filtered) { filtered.chunked(2) }
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    rows.forEach { row ->
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            row.forEach { p ->
                                val pid = p.id ?: 0L
                                ProductCardRemote(
                                    p = p,
                                    onAdd = { onAddToCart(p) },
                                    onOpen = { onOpenProduct(pid.toString()) },
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
private fun ProductCardRemote(
    p: ProductDtoRemote,
    onAdd: () -> Unit,
    onOpen: () -> Unit,
    modifier: Modifier = Modifier
) {
    val nombre = p.nombre.orEmpty()
    val precio = p.precio ?: 0.0
    val stock = p.stock ?: 0
    val img = p.imagenUrl

    ElevatedCard(
        modifier = modifier.clickable(onClick = onOpen),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(Modifier.padding(12.dp)) {

            if (!img.isNullOrBlank()) {
                AsyncImage(
                    model = img,
                    contentDescription = nombre,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Crop
                )
                Spacer(Modifier.height(8.dp))
            }

            Text(
                nombre,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                fontWeight = FontWeight.SemiBold
            )

            Text(
                "$ ${"%,.0f".format(precio)}",
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(Modifier.height(4.dp))
            if (stock <= 0) {
                AssistChip(onClick = {}, label = { Text("Agotado") })
            } else {
                AssistChip(onClick = {}, label = { Text("Stock: $stock") })
            }

            Spacer(Modifier.height(10.dp))
            Button(
                onClick = onAdd,
                enabled = stock > 0,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Filled.AddShoppingCart, contentDescription = null)
                Spacer(Modifier.width(6.dp))
                Text(if (stock > 0) "Agregar" else "Agotado")
            }
        }
    }
}
