package com.example.bicypower.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp

@Composable
fun ProfileScreen() {
    Column(
        Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(60.dp).clip(CircleShape), contentAlignment = Alignment.Center) {
                Text("J", style = MaterialTheme.typography.titleLarge)
            }
            Spacer(Modifier.width(12.dp))
            Column {
                Text("José Oporto", style = MaterialTheme.typography.titleMedium)
                Text("jose@example.com", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        ElevatedCard {
            ListItem(headlineContent = { Text("Mis pedidos") })
        }
        ElevatedCard {
            ListItem(headlineContent = { Text("Direcciones") })
        }
        ElevatedCard {
            ListItem(headlineContent = { Text("Métodos de pago") })
        }
        ElevatedCard {
            ListItem(headlineContent = { Text("Cerrar sesión") })
        }
    }
}
