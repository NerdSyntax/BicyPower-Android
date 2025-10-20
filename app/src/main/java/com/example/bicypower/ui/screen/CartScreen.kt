package com.example.bicypower.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.bicypower.data.CartStore

@Composable
fun CartScreen(onCheckout: () -> Unit = {}) {
    val items by CartStore.items.collectAsState()
    val detailed = remember(items) { CartStore.detailed() }
    val total = remember(items) { CartStore.total() }

    if (detailed.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Tu carrito está vacío 🙃")
        }
        return
    }

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("Carrito", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold)
        Spacer(Modifier.height(12.dp))

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(detailed, key = { it.product.id }) { row ->
                ElevatedCard {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text(row.product.name, fontWeight = FontWeight.SemiBold)
                            Text("$ ${"%,.0f".format(row.product.price)} c/u", style = MaterialTheme.typography.labelMedium)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = {
                                val n = row.quantity - 1
                                if (n <= 0) CartStore.remove(row.product.id) else CartStore.set(row.product.id, n)
                            }) { Icon(Icons.Filled.Remove, contentDescription = "menos") }

                            Text("${row.quantity}", modifier = Modifier.width(28.dp), textAlign = TextAlign.Center)

                            IconButton(onClick = { CartStore.set(row.product.id, row.quantity + 1) }) {
                                Icon(Icons.Filled.Add, contentDescription = "más")
                            }

                            IconButton(onClick = { CartStore.remove(row.product.id) }) {
                                Icon(Icons.Filled.Delete, contentDescription = "quitar")
                            }
                        }
                    }
                }
            }
        }

        Divider()
        Spacer(Modifier.height(8.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Total")
            Text("$ ${"%,.0f".format(total)}", fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(8.dp))
        Button(onClick = onCheckout, modifier = Modifier.fillMaxWidth()) {
            Text("Ir a pagar")
        }
    }
}
