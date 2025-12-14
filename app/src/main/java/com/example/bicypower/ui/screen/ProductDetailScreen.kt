package com.example.bicypower.ui.screen

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material3.BottomAppBarDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.bicypower.data.CartStore
import com.example.bicypower.data.remote.BicyPowerRemoteModule
import com.example.bicypower.data.remote.dto.ProductDtoRemote
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDetailScreen(
    productId: String,
    onBack: () -> Unit,
    onGoToCart: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val api = remember { BicyPowerRemoteModule.productsApi }

    val idLong = remember(productId) { productId.toLongOrNull() }

    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var product by remember { mutableStateOf<ProductDtoRemote?>(null) }

    LaunchedEffect(idLong) {
        isLoading = true
        error = null
        product = null

        if (idLong == null) {
            isLoading = false
            return@LaunchedEffect
        }

        runCatching {
            val resp = api.getProducts()
            if (!resp.isSuccessful || resp.body() == null) {
                throw Exception("Error ${resp.code()} cargando producto")
            }
            resp.body()!!.firstOrNull { it.id == idLong }
        }.onSuccess {
            product = it
        }.onFailure {
            error = it.message
        }

        isLoading = false
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = product?.nombre ?: "Detalle",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
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
            val p = product
            if (p != null) {
                BottomActionBarRemote(
                    product = p,
                    onAdd = {
                        val pid = p.id ?: return@BottomActionBarRemote

                        val currentStock = p.stock ?: 0
                        if (currentStock <= 0) {
                            Toast.makeText(context, "Producto agotado", Toast.LENGTH_SHORT).show()
                            return@BottomActionBarRemote
                        }

                        scope.launch {
                            runCatching {
                                val updated = p.copy(stock = (currentStock - 1).coerceAtLeast(0))
                                val resp = api.updateProduct(pid, updated)
                                if (!resp.isSuccessful || resp.body() == null) {
                                    throw Exception("Error ${resp.code()} actualizando stock")
                                }
                                resp.body()!!
                            }.onSuccess { serverUpdated ->
                                product = serverUpdated
                                CartStore.add(serverUpdated)
                                Toast.makeText(context, "Agregado al carrito", Toast.LENGTH_SHORT).show()
                            }.onFailure {
                                Toast.makeText(
                                    context,
                                    it.message ?: "No se pudo agregar",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    },
                    onGoToCart = onGoToCart
                )
            }
        }
    ) { inner ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(inner)
        ) {
            when {
                isLoading -> CircularProgressIndicator(Modifier.align(Alignment.Center))
                error != null -> Text("Error: $error", Modifier.align(Alignment.Center))
                idLong == null || product == null -> NotFound(onBack)
                else -> DetailContentRemote(product!!, Modifier.fillMaxSize())
            }
        }
    }
}

@Composable
private fun NotFound(onBack: () -> Unit) {
    Column(
        Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Producto no encontrado")
        Spacer(Modifier.height(12.dp))
        OutlinedButton(onClick = onBack) { Text("Volver") }
    }
}

@Composable
private fun DetailContentRemote(p: ProductDtoRemote, modifier: Modifier = Modifier) {
    val nombre = p.nombre.orEmpty()
    val precio = p.precio ?: 0.0
    val stock = p.stock ?: 0
    val img = p.imagenUrl.orEmpty()
    val activo = p.activo ?: true
    val descripcion = p.descripcion

    Column(
        modifier
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        if (img.isNotBlank()) {
            AsyncImage(
                model = img,
                contentDescription = nombre,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp),
                contentScale = ContentScale.Crop
            )
            Spacer(Modifier.height(16.dp))
        }

        Text(nombre, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(6.dp))
        Text(
            text = "$ ${"%,.0f".format(precio)}",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (stock > 0) {
                AssistChip(onClick = {}, label = { Text("Stock: $stock") })
            } else {
                AssistChip(onClick = {}, label = { Text("Agotado") })
            }

            if (!activo) {
                Spacer(Modifier.width(8.dp))
                AssistChip(onClick = {}, label = { Text("Inactivo") })
            }
        }

        if (!descripcion.isNullOrBlank()) {
            Spacer(Modifier.height(16.dp))
            Text(
                "Descripción",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(6.dp))
            Text(descripcion, style = MaterialTheme.typography.bodyMedium)
        }

        Spacer(Modifier.height(90.dp))
    }
}

@Composable
private fun BottomActionBarRemote(
    product: ProductDtoRemote,
    onAdd: () -> Unit,
    onGoToCart: () -> Unit
) {
    val stock = product.stock ?: 0

    Surface(tonalElevation = BottomAppBarDefaults.ContainerElevation) {
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
                enabled = stock > 0
            ) { Text(if (stock > 0) "Agregar" else "Agotado") }
        }
    }
}
