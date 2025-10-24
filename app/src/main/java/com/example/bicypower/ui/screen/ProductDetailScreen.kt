package com.example.bicypower.ui.screen

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.bicypower.data.CartStore
import com.example.bicypower.data.local.database.BicyPowerDatabase
import com.example.bicypower.data.local.product.ProductEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDetailScreen(
    productId: String,
    onBack: () -> Unit,
    onGoToCart: () -> Unit
) {
    val context = LocalContext.current
    val dao = remember { BicyPowerDatabase.getInstance(context).productDao() }
    val idLong = remember(productId) { productId.toLongOrNull() }

    // Observa el producto por id. Si no tienes observeById en el DAO,
    // derivamos desde observeAll() para no tocar tu DAO.
    val product by remember(idLong) {
        dao.observeAll().map { list -> list.firstOrNull { it.id == idLong } }
    }.collectAsState(initial = null)

    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = product?.name ?: "Detalle",
                        maxLines = 1, overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    IconButton(onClick = onGoToCart) {
                        Icon(Icons.Filled.ShoppingCart, contentDescription = "Carrito")
                    }
                }
            )
        },
        bottomBar = {
            if (product != null) {
                BottomActionBar(
                    product = product!!,
                    onAdd = {
                        val p = product ?: return@BottomActionBar
                        if (p.stock <= 0) {
                            Toast.makeText(context, "Producto agotado", Toast.LENGTH_SHORT).show()
                            return@BottomActionBar
                        }
                        scope.launch {
                            // 1) agrega al carrito (sigues usando tu CartStore actual)
                            CartStore.addDb(p)
                            // 2) descuenta stock en Room
                            runCatching {
                                withContext(Dispatchers.IO) {
                                    dao.updateStock(p.id, p.stock - 1)
                                }
                            }.onSuccess {
                                Toast.makeText(context, "Agregado al carrito", Toast.LENGTH_SHORT).show()
                            }.onFailure {
                                Toast.makeText(context, it.message ?: "Error al actualizar stock", Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    onGoToCart = onGoToCart
                )
            }
        }
    ) { inner ->
        when {
            idLong == null -> NotFound(inner, onBack)
            product == null -> NotFound(inner, onBack)
            else -> DetailContent(product!!, Modifier.padding(inner))
        }
    }
}

@Composable
private fun NotFound(padding: PaddingValues, onBack: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Producto no encontrado")
            Spacer(Modifier.height(12.dp))
            OutlinedButton(onClick = onBack) { Text("Volver") }
        }
    }
}

@Composable
private fun DetailContent(p: ProductEntity, modifier: Modifier = Modifier) {
    Column(
        modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        if (p.imageUrl.isNotBlank()) {
            AsyncImage(
                model = p.imageUrl,
                contentDescription = p.name,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp),
                contentScale = ContentScale.Crop
            )
            Spacer(Modifier.height(16.dp))
        }

        Text(p.name, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(6.dp))
        Text(
            text = "$ ${"%,.0f".format(p.price)}",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (p.stock > 0) {
                AssistChip(onClick = {}, label = { Text("Stock: ${p.stock}") })
            } else {
                AssistChip(onClick = {}, label = { Text("Agotado") })
            }
            if (!p.active) {
                Spacer(Modifier.width(8.dp))
                AssistChip(onClick = {}, label = { Text("Inactivo") })
            }
        }

        if (p.description.isNotBlank()) {
            Spacer(Modifier.height(16.dp))
            Text("Descripción", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(6.dp))
            Text(p.description, style = MaterialTheme.typography.bodyMedium)
        }

        Spacer(Modifier.height(90.dp)) // deja espacio para la bottom bar
    }
}

@Composable
private fun BottomActionBar(
    product: ProductEntity,
    onAdd: () -> Unit,
    onGoToCart: () -> Unit
) {
    Surface(tonalElevation = 2.dp) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                modifier = Modifier.weight(1f),
                onClick = onGoToCart
            ) { Text("Ir al carrito") }

            Button(
                modifier = Modifier.weight(1f),
                onClick = onAdd,
                enabled = product.stock > 0
            ) { Text(if (product.stock > 0) "Agregar" else "Agotado") }
        }
    }
}
