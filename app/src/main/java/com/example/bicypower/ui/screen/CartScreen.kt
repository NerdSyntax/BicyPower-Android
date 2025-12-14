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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.bicypower.data.CartStore
import com.example.bicypower.data.local.session.UserSession
import com.example.bicypower.data.remote.dto.PurchaseItemRequestDtoRemote
import com.example.bicypower.data.remote.dto.PurchaseRequestDtoRemote
import com.example.bicypower.data.repository.CheckoutState
import com.example.bicypower.data.repository.PurchaseRepository
import kotlinx.coroutines.launch

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

    // Loading al confirmar
    var isSubmitting by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Sesión real (userId real)
    val session = remember { UserSession(context) }
    val userId by session.userId.collectAsState(initial = 0L)

    // Repo compras (ms-compras)
    val purchaseRepo = remember { PurchaseRepository() }

    if (detailed.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Tu carrito está vacío")
        }
        return
    }

    Column(
        Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            "Carrito",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.ExtraBold
        )
        Spacer(Modifier.height(12.dp))

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(
                items = detailed,
                key = { it.product.id ?: -1L } // <- nunca null
            ) { row ->

                val pid = row.product.id ?: return@items
                val nombre = row.product.nombre.orEmpty()
                val precioUnit = row.product.precio ?: 0.0

                ElevatedCard {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text(nombre, fontWeight = FontWeight.SemiBold)
                            Text(
                                "$ ${"%,.0f".format(precioUnit)} c/u",
                                style = MaterialTheme.typography.labelMedium
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = {
                                val n = row.quantity - 1
                                if (n <= 0) CartStore.remove(pid) else CartStore.set(pid, n)
                            }) {
                                Icon(Icons.Filled.Remove, contentDescription = "menos")
                            }

                            Text(
                                "${row.quantity}",
                                modifier = Modifier.width(28.dp),
                                textAlign = TextAlign.Center
                            )

                            IconButton(onClick = { CartStore.set(pid, row.quantity + 1) }) {
                                Icon(Icons.Filled.Add, contentDescription = "más")
                            }

                            IconButton(onClick = { CartStore.remove(pid) }) {
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
                if (userId <= 0L) {
                    Toast.makeText(context, "Sesión inválida (userId=0). Vuelve a iniciar sesión.", Toast.LENGTH_LONG).show()
                    return@Button
                }

                if (!hasAddress || !hasPayment) {
                    Toast.makeText(
                        context,
                        "Para pagar debes registrar una dirección y un método de pago en tu perfil.",
                        Toast.LENGTH_LONG
                    ).show()
                    return@Button
                }

                if (addresses.isEmpty()) {
                    Toast.makeText(context, "No tienes direcciones guardadas.", Toast.LENGTH_LONG).show()
                    return@Button
                }

                if (selectedAddressId == null) {
                    Toast.makeText(context, "Selecciona una dirección de envío.", Toast.LENGTH_LONG).show()
                    return@Button
                }

                showConfirm = true
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isSubmitting
        ) {
            Text(if (isSubmitting) "Procesando..." else "Ir a pagar")
        }
    }

    if (showConfirm) {
        val address = addresses.firstOrNull { it.id == selectedAddressId } ?: addresses.first()
        val shippingDays = 3

        AlertDialog(
            onDismissRequest = { if (!isSubmitting) showConfirm = false },
            title = { Text("Confirmar compra") },
            text = {
                Column {
                    Text("Total a pagar: $ ${"%,.0f".format(total)}")
                    Spacer(Modifier.height(4.dp))
                    Text("Envío a:")
                    Text(
                        "${address.linea1}" + if (address.linea2.isNotBlank()) ", ${address.linea2}" else "",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text("${address.ciudad}, ${address.region}", style = MaterialTheme.typography.bodyMedium)
                    Spacer(Modifier.height(4.dp))
                    Text("Entrega estimada: $shippingDays días")
                    Spacer(Modifier.height(8.dp))
                    Text("¿Estás seguro de que quieres realizar esta compra?")
                }
            },
            confirmButton = {
                Button(
                    enabled = !isSubmitting,
                    onClick = {
                        if (userId <= 0L) {
                            Toast.makeText(context, "Sesión inválida (userId=0).", Toast.LENGTH_LONG).show()
                            return@Button
                        }

                        val req = PurchaseRequestDtoRemote(
                            usuarioId = userId,
                            items = detailed.map { row ->
                                PurchaseItemRequestDtoRemote(
                                    productoId = row.product.id ?: 0L,
                                    cantidad = row.quantity,
                                    precioUnitario = (row.product.precio ?: 0.0)
                                )
                            }
                        )

                        scope.launch {
                            isSubmitting = true
                            try {
                                val resp = purchaseRepo.create(req) // POST REAL al ms-compras

                                // vaciar carrito SOLO si el POST fue OK
                                detailed.forEach { row ->
                                    val pid = row.product.id ?: return@forEach
                                    CartStore.remove(pid)
                                }

                                Toast.makeText(
                                    context,
                                    "Compra OK (#${resp.id}). Llega en aprox. $shippingDays días.",
                                    Toast.LENGTH_LONG
                                ).show()

                                showConfirm = false
                                onCheckout()

                            } catch (e: Exception) {
                                Toast.makeText(
                                    context,
                                    "Error al guardar compra: ${e.message}",
                                    Toast.LENGTH_LONG
                                ).show()
                            } finally {
                                isSubmitting = false
                            }
                        }
                    }
                ) {
                    Text("Confirmar compra")
                }
            },
            dismissButton = {
                Button(enabled = !isSubmitting, onClick = { showConfirm = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}
