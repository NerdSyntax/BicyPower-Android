package com.example.bicypower.ui.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.bicypower.data.CartStore
import com.example.bicypower.data.Catalog
import com.example.bicypower.data.Product

@Composable
fun HomeScreen(
    onOpenProduct: (String) -> Unit = {},
    onAddToCart: (Product) -> Unit = {}
) {
    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("BicyPower", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold)
        Spacer(Modifier.height(12.dp))

        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 160.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(Catalog.all, key = { it.id }) { p ->
                ProductCard(
                    product = p,
                    onClick = { onOpenProduct(p.id) },
                    onAdd = { onAddToCart(p) }
                )
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
