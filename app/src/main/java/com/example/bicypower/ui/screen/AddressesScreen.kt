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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.bicypower.data.repository.Address
import com.example.bicypower.data.repository.CheckoutState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddressesScreen(onBack: () -> Unit) {
    // Leemos las direcciones desde el estado global
    val items by CheckoutState.addresses.collectAsState()

    var nombre  by remember { mutableStateOf("") }
    var linea1 by remember { mutableStateOf("") }
    var linea2 by remember { mutableStateOf("") }
    var ciudad by remember { mutableStateOf("") }
    var region by remember { mutableStateOf("") }
    var zip by remember { mutableStateOf("") }
    var telefono by remember { mutableStateOf("") }

    val canSave = nombre.isNotBlank() && linea1.isNotBlank() && ciudad.isNotBlank() &&
            region.isNotBlank() && zip.isNotBlank() && telefono.isNotBlank()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Direcciones") },
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
                        value = nombre,
                        onValueChange = { nombre = it },
                        label = { Text("Nombre de receptor") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = linea1,
                        onValueChange = { linea1 = it },
                        label = { Text("Calle y número") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = linea2,
                        onValueChange = { linea2 = it },
                        label = { Text("Depto, piso (opcional)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = ciudad,
                            onValueChange = { ciudad = it },
                            label = { Text("Ciudad") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = region,
                            onValueChange = { region = it },
                            label = { Text("Región/Estado") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = zip,
                            onValueChange = { zip = it },
                            label = { Text("Código postal") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = telefono,
                            onValueChange = { telefono = it.filter(Char::isDigit) },
                            label = { Text("Teléfono") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Spacer(Modifier.height(12.dp))
                    Button(
                        onClick = {
                            CheckoutState.addAddress(
                                Address(
                                    id = System.currentTimeMillis(),
                                    nombre = nombre.trim(),
                                    linea1 = linea1.trim(),
                                    linea2 = linea2.trim(),
                                    ciudad = ciudad.trim(),
                                    region = region.trim(),
                                    zip = zip.trim(),
                                    telefono = telefono.trim()
                                )
                            )
                            nombre = ""; linea1 = ""; linea2 = ""
                            ciudad = ""; region = ""; zip = ""; telefono = ""
                        },
                        enabled = canSave,
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("Guardar dirección") }
                }
            }

            Spacer(Modifier.height(16.dp))
            Text("Guardadas", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))

            if (items.isEmpty()) {
                Text("No tienes direcciones guardadas.")
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(items, key = { it.id }) { a ->
                        ElevatedCard {
                            ListItem(
                                headlineContent = {
                                    Text(
                                        "${a.nombre} - ${a.linea1}" +
                                                if (a.linea2.isNotBlank()) ", ${a.linea2}" else ""
                                    )
                                },
                                supportingContent = {
                                    Text("${a.ciudad}, ${a.region} • ${a.zip} • ${a.telefono}")
                                },
                                trailingContent = {
                                    IconButton(onClick = {
                                        CheckoutState.removeAddress(a.id)
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
