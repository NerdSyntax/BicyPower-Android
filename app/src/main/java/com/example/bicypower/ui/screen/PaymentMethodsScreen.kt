package com.example.bicypower.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.bicypower.data.repository.CheckoutState

data class PaymentMethod(
    val id: Long,
    val titular: String,
    val numero: String,
    val expMonth: String,
    val expYear: String,
    val cvv: String
)

private fun maskCard(number: String): String {
    val cleaned = number.filter { it.isDigit() }
    return if (cleaned.length >= 4) "**** **** **** ${cleaned.takeLast(4)}" else "****"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentMethodsScreen(onBack: () -> Unit) {
    val cards = remember { mutableStateListOf<PaymentMethod>() }

    var titular by remember { mutableStateOf("") }
    var numero by remember { mutableStateOf("") }
    var month by remember { mutableStateOf("") }
    var year by remember { mutableStateOf("") }
    var cvv by remember { mutableStateOf("") }

    val canSave = titular.isNotBlank() &&
            numero.filter { it.isDigit() }.length in 13..19 &&
            month.length == 2 && month.toIntOrNull() in 1..12 &&
            year.length == 2 && cvv.length in 3..4

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Métodos de pago") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { inner ->
        Column(Modifier.padding(inner).padding(16.dp)) {
            ElevatedCard(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(12.dp)) {
                    OutlinedTextField(
                        value = titular,
                        onValueChange = { titular = it },
                        label = { Text("Titular") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = numero,
                        onValueChange = { numero = it.filter(Char::isDigit) },
                        label = { Text("Número de tarjeta") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = month,
                            onValueChange = { month = it.filter(Char::isDigit).take(2) },
                            label = { Text("MM") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = year,
                            onValueChange = { year = it.filter(Char::isDigit).take(2) },
                            label = { Text("AA") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = cvv,
                            onValueChange = { cvv = it.filter(Char::isDigit).take(4) },
                            label = { Text("CVV") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Spacer(Modifier.height(12.dp))
                    Button(
                        onClick = {
                            cards.add(
                                PaymentMethod(
                                    id = System.currentTimeMillis(),
                                    titular = titular.trim(),
                                    numero = numero.filter(Char::isDigit),
                                    expMonth = month,
                                    expYear = year,
                                    cvv = cvv
                                )
                            )
                            // avisamos al estado global cuántas tarjetas hay
                            CheckoutState.updatePaymentCount(cards.size)

                            titular = ""; numero = ""; month = ""; year = ""; cvv = ""
                        },
                        enabled = canSave,
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("Agregar tarjeta") }
                }
            }

            Spacer(Modifier.height(16.dp))
            Text("Guardadas", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))

            if (cards.isEmpty()) {
                Text("No tienes métodos de pago guardados.")
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(cards, key = { it.id }) { c ->
                        ElevatedCard {
                            ListItem(
                                headlineContent = { Text(maskCard(c.numero)) },
                                supportingContent = {
                                    Text("${c.titular}  •  Exp ${c.expMonth}/${c.expYear}")
                                },
                                trailingContent = {
                                    IconButton(onClick = {
                                        cards.removeAll { it.id == c.id }
                                        CheckoutState.updatePaymentCount(cards.size)
                                    }) {
                                        Icon(Icons.Filled.Delete, contentDescription = "Eliminar")
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
