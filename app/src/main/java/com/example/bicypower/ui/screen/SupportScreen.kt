package com.example.bicypower.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun SupportScreen() {
    Column(
        Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Soporte", style = MaterialTheme.typography.titleLarge)

        ElevatedCard {
            ListItem(
                leadingContent = { Icon(Icons.Default.Chat, contentDescription = null) },
                headlineContent = { Text("Chat en vivo") },
                supportingContent = { Text("Tiempo de respuesta ~5 min") }
            )
        }
        ElevatedCard {
            ListItem(
                leadingContent = { Icon(Icons.Default.Phone, contentDescription = null) },
                headlineContent = { Text("Llámanos") },
                supportingContent = { Text("+56 2 1234 5678") }
            )
        }
        ElevatedCard {
            ListItem(
                leadingContent = { Icon(Icons.Default.Email, contentDescription = null) },
                headlineContent = { Text("Correo") },
                supportingContent = { Text("support@bicypower.cl") }
            )
        }
    }
}
