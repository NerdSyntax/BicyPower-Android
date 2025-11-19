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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.bicypower.data.CartStore
import com.example.bicypower.data.repository.CheckoutState
import com.example.bicypower.data.repository.Order
import com.example.bicypower.data.repository.OrderStore

@Composable
fun CartScreen(onCheckout: () -> Unit = {}) {
    val items by CartStore.items.collectAsState()
    val detailed = remember(items) { CartStore.detailed() }
    val total = remember(items) { CartStore.total() }

    // Estado global: direcciones y flags
    val hasAddress by CheckoutState.hasAddress.collectAsState()
    val hasPayment by CheckoutState.hasPayment.collectAsState()
    val addresses by CheckoutState.addresses.collectAsState()

    // Dirección seleccionada
    var selectedAddressId by remember { mutableStateOf<Long?>(null) }
    LaunchedEffect(addresses) {
        if (addresses.isNotEmpty() && selectedAddressId == null) {
            selectedAddressId = addresses.first().id
        }
    }

    // Diálogo de confirmación
    var showConfirm by remember { mutableStateOf(false) }

    val context = LocalContext.current

    if (detailed.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Tu carrito está vacío ")
        }
        return
    }

    // ====== CONTENIDO PRINCIPAL ======
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
                            Text(
                                "$ ${"%,.0f".format(row.product.price)} c/u",
                                style = MaterialTheme.typography.labelMedium
                            )
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = {
                                val n = row.quantity - 1
                                if (n <= 0) CartStore.remove(row.product.id) else CartStore.set(row.product.id, n)
                            }) { Icon(Icons.Filled.Remove, contentDescription = "menos") }

                            Text(
                                "${row.quantity}",
                                modifier = Modifier.width(28.dp),
                                textAlign = TextAlign.Center
                            )

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

        Spacer(Modifier.height(12.dp))

        // ===== Dirección de envío seleccionable =====
        Text("Dirección de envío", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(4.dp))

        if (addresses.isEmpty()) {
            Text(
                "No tienes direcciones guardadas. Agrega una en Perfil > Direcciones.",
                style = MaterialTheme.typography.bodySmall
            )
        } else {
            Column {
                addresses.forEach { a ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        RadioButton(
                            selected = (a.id == selectedAddressId),
                            onClick = { selectedAddressId = a.id }
                        )
                        Spacer(Modifier.width(4.dp))
                        Column {
                            Text(a.linea1 + if (a.linea2.isNotBlank()) ", ${a.linea2}" else "")
                            Text("${a.ciudad}, ${a.region}", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(8.dp))

        Button(
            onClick = {
                // Validación básica antes del diálogo
                if (!hasAddress || !hasPayment) {
                    Toast.makeText(
                        context,
                        "Para pagar debes registrar una dirección y un método de pago en tu perfil.",
                        Toast.LENGTH_LONG
                    ).show()
                    return@Button
                }

                if (addresses.isEmpty()) {
                    Toast.makeText(
                        context,
                        "No tienes direcciones guardadas.",
                        Toast.LENGTH_LONG
                    ).show()
                    return@Button
                }

                if (selectedAddressId == null) {
                    Toast.makeText(
                        context,
                        "Selecciona una dirección de envío.",
                        Toast.LENGTH_LONG
                    ).show()
                    return@Button
                }

                // Si todo está ok, mostramos el diálogo de confirmación
                showConfirm = true
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Ir a pagar")
        }
    }

    // ====== DIÁLOGO DE CONFIRMACIÓN ======
    if (showConfirm) {
        val address = addresses.firstOrNull { it.id == selectedAddressId } ?: addresses.first()
        val shippingDays = 3 // puedes cambiarlo o calcularlo

        AlertDialog(
            onDismissRequest = { showConfirm = false },
            title = { Text("Confirmar compra") },
            text = {
                Column {
                    Text("Total a pagar: $ ${"%,.0f".format(total)}")
                    Spacer(Modifier.height(4.dp))
                    Text("Envío a:")
                    Text(
                        "${address.linea1}" +
                                if (address.linea2.isNotBlank()) ", ${address.linea2}" else "",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        "${address.ciudad}, ${address.region}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(Modifier.height(4.dp))
                    Text("Entrega estimada: $shippingDays días")
                    Spacer(Modifier.height(8.dp))
                    Text("¿Estás seguro de que quieres realizar esta compra?")
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        // Realizamos la compra aquí
                        val shippingDays = 3

                        val order = Order(
                            id = System.currentTimeMillis(),
                            total = total,
                            itemsCount = detailed.sumOf { it.quantity },
                            createdAt = System.currentTimeMillis(),
                            addressSummary = "${address.linea1}, ${address.ciudad}",
                            shippingDays = shippingDays
                        )
                        OrderStore.add(order)

                        detailed.forEach { row ->
                            CartStore.remove(row.product.id)
                        }

                        Toast.makeText(
                            context,
                            "Compra realizada con éxito. Llega en aproximadamente $shippingDays días.",
                            Toast.LENGTH_LONG
                        ).show()

                        showConfirm = false
                        onCheckout()
                    }
                ) {
                    Text("Confirmar compra")
                }
            },
            dismissButton = {
                Button(onClick = { showConfirm = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}
