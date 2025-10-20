package com.example.bicypower.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.bicypower.data.Catalog
import com.example.bicypower.data.CartStore

@Composable
fun ProductDetailScreen(
    productId: String,
    onAddDoneGoCart: () -> Unit = {}   // si quieres ir directo al carrito
) {
    val product = remember(productId) { Catalog.byId(productId) }
    var qty by remember { mutableStateOf(1) }

    if (product == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Producto no encontrado")
        }
        return
    }

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text(product.name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Text("$ ${"%,.0f".format(product.price)}", color = MaterialTheme.colorScheme.primary)

        Spacer(Modifier.height(16.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(MaterialTheme.colorScheme.secondaryContainer),
            contentAlignment = Alignment.Center
        ) { Text(product.emoji, style = MaterialTheme.typography.displaySmall) }

        Spacer(Modifier.height(16.dp))
        Text(product.description)

        Spacer(Modifier.height(16.dp))
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            IconButton(onClick = { if (qty > 1) qty-- }) { Icon(Icons.Filled.Remove, contentDescription = "Menos") }
            Text("$qty", style = MaterialTheme.typography.titleMedium)
            IconButton(onClick = { qty++ }) { Icon(Icons.Filled.Add, contentDescription = "Más") }
        }

        Spacer(Modifier.height(16.dp))
        Button(
            onClick = {
                CartStore.add(product.id, qty)
                onAddDoneGoCart()
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp)
        ) { Text("Añadir al carrito") }
    }
}
