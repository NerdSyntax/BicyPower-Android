package com.example.bicypower.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.bicypower.data.local.product.ProductEntity
import com.example.bicypower.ui.viewmodel.HomeViewModel

// --------- MODELO SIMPLE PARA MENSAJES ----------
data class StaffChatPreview(
    val id: Long,
    val clientName: String,
    val lastMessage: String,
    val unread: Int
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StaffHomeScreen(
    onLogout: () -> Unit
) {
    val vm: HomeViewModel = viewModel()
    val state by vm.state.collectAsState()

    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Mensajes", "Productos")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Panel Staff") },
                actions = {
                    TextButton(onClick = onLogout) {
                        Text("Cerrar sesión")
                    }
                }
            )
        }
    ) { inner ->
        Column(
            modifier = Modifier
                .padding(inner)
                .padding(16.dp)
                .fillMaxSize()
        ) {
            TabRow(selectedTabIndex = selectedTab) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title) }
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            when (selectedTab) {
                0 -> StaffMessagesSection()
                1 -> StaffProductsSection(
                    products = state.items,
                    isLoading = state.isLoading
                )
            }
        }
    }
}

@Composable
private fun StaffMessagesSection() {
    // Mensajes de ejemplo (no hay backend)
    val chats = remember {
        listOf(
            StaffChatPreview(
                id = 1,
                clientName = "Juan Pérez",
                lastMessage = "Hola, mi pedido aún no llega.",
                unread = 2
            ),
            StaffChatPreview(
                id = 2,
                clientName = "María López",
                lastMessage = "¿Pueden cambiar la dirección de envío?",
                unread = 0
            ),
            StaffChatPreview(
                id = 3,
                clientName = "Carlos Sánchez",
                lastMessage = "Gracias por la ayuda 🙂",
                unread = 0
            )
        )
    }

    if (chats.isEmpty()) {
        Text("No hay mensajes de clientes por ahora.")
    } else {
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(chats, key = { it.id }) { chat ->
                ElevatedCard {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text(chat.clientName, fontWeight = FontWeight.SemiBold)
                            Text(chat.lastMessage, style = MaterialTheme.typography.bodySmall)
                        }
                        if (chat.unread > 0) {
                            AssistChip(
                                onClick = { /* abrir chat en un futuro */ },
                                label = { Text("${chat.unread} nuevos") }
                            )
                        } else {
                            Text("Leído", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }
        }
        Spacer(Modifier.height(8.dp))
        Text(
            "Aquí el staff puede revisar y responder mensajes de los clientes.",
            style = MaterialTheme.typography.bodySmall
        )
    }
}

@Composable
private fun StaffProductsSection(
    products: List<ProductEntity>,
    isLoading: Boolean
) {
    if (isLoading) {
        Box(
            Modifier
                .fillMaxWidth()
                .height(120.dp),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }

    if (products.isEmpty()) {
        Text("No hay productos cargados en el catálogo.")
        return
    }

    Text("Catálogo de productos", style = MaterialTheme.typography.titleMedium)
    Spacer(Modifier.height(8.dp))

    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items(products, key = { it.id }) { p ->
            ElevatedCard {
                Column(Modifier.padding(12.dp)) {
                    Text(p.name, fontWeight = FontWeight.SemiBold)
                    Text("$ ${"%,.0f".format(p.price)}")
                    Spacer(Modifier.height(4.dp))
                    Text(
                        if (p.stock <= 0) "Sin stock"
                        else "Stock disponible: ${p.stock}",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}
