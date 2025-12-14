package com.example.bicypower.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.bicypower.ui.viewmodel.OrdersViewModel
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrdersScreen(
    onBack: () -> Unit,
    userId: Long,
    vm: OrdersViewModel = viewModel()
) {
    val state by vm.state.collectAsState()
    val nf = remember { NumberFormat.getNumberInstance(Locale("es", "CL")) }

    LaunchedEffect(userId) {
        if (userId > 0) vm.loadMyOrders(userId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mis pedidos") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { inner ->
        Column(
            Modifier
                .padding(inner)
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            when {
                state.isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }

                state.errorMsg != null -> Text(
                    "Error: ${state.errorMsg}",
                    color = MaterialTheme.colorScheme.error
                )

                userId <= 0L -> Text("No hay sesión válida (userId=0).")

                state.orders.isEmpty() -> Text(
                    "Aún no tienes pedidos. Cuando confirmes una compra, aparecerá aquí.",
                    style = MaterialTheme.typography.bodyMedium
                )

                else -> LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(state.orders, key = { it.id }) { o ->
                        ElevatedCard(Modifier.fillMaxWidth()) {
                            Column(Modifier.padding(12.dp)) {
                                Text("Pedido #${o.id}", style = MaterialTheme.typography.titleMedium)
                                Text("Fecha: ${o.fecha}", style = MaterialTheme.typography.bodySmall)
                                Spacer(Modifier.height(4.dp))
                                Text("Total: $ ${nf.format(o.total)}")
                                Text("Ítems: ${o.items.size}")
                                Spacer(Modifier.height(8.dp))
                                Text("Detalle:", style = MaterialTheme.typography.labelLarge)
                                o.items.forEach { it ->
                                    Text("• Prod ${it.productoId} x${it.cantidad} ($ ${nf.format(it.precioUnitario)})")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
