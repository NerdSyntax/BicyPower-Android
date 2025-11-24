package com.example.bicypower.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckBikeScreen(
    onBack: () -> Unit
) {
    var serial by remember { mutableStateOf("") }
    var result by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Revisar bicicleta") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                }
            )
        }
    ) { inner ->
        Column(
            modifier = Modifier
                .padding(inner)
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "Ingresa el número de serie de la bicicleta para consultar en la API externa.",
                style = MaterialTheme.typography.bodyMedium
            )

            OutlinedTextField(
                value = serial,
                onValueChange = { serial = it },
                label = { Text("Número de serie") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Button(
                onClick = {
                    if (serial.isBlank()) {
                        error = "Debes ingresar un número de serie"
                        result = null
                        return@Button
                    }

                    error = null
                    result = null
                    isLoading = true

                    scope.launch {
                        try {
                            // 🔹 AQUÍ deberías llamar a tu API real
                            // Por ahora simulo un delay de red:
                            delay(1500)

                            // Ejemplo súper simple:
                            result = if (serial.endsWith("9")) {
                                "⚠ Bicicleta reportada como robada."
                            } else {
                                "✅ Bicicleta sin reportes de robo."
                            }
                        } catch (e: Exception) {
                            error = e.message ?: "Error al consultar API externa"
                        } finally {
                            isLoading = false
                        }
                    }
                },
                enabled = !isLoading,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (isLoading) "Consultando..." else "Consultar")
            }

            if (isLoading) {
                CircularProgressIndicator()
            }

            error?.let {
                Text(
                    it,
                    color = MaterialTheme.colorScheme.error
                )
            }

            result?.let {
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("Resultado", fontWeight = FontWeight.Bold)
                        Text(it)
                    }
                }
            }
        }
    }
}
