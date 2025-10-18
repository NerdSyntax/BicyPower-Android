
package com.example.bicypower.ui.screen

import android.app.Activity
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

@Composable
fun SettingsScreen(
    onLogout: () -> Unit = {}
) {
    var darkMode by remember { mutableStateOf(false) }
    var notifications by remember { mutableStateOf(true) }
    val activity = LocalContext.current as? Activity
    var showLogoutDialog by remember { mutableStateOf(false) }

    Column(Modifier.padding(16.dp)) {
        Text("Settings", style = MaterialTheme.typography.titleLarge)

        ListItem(
            headlineContent = { Text("Modo oscuro") },
            trailingContent = { Switch(checked = darkMode, onCheckedChange = { darkMode = it }) }
        )
        ListItem(
            headlineContent = { Text("Notificaciones") },
            trailingContent = { Switch(checked = notifications, onCheckedChange = { notifications = it }) }
        )

        Divider()

        Button(
            onClick = { showLogoutDialog = true },
            modifier = Modifier.padding(top = 12.dp)
        ) { Text("Cerrar sesión") }

        OutlinedButton(
            onClick = { activity?.finishAffinity() },
            modifier = Modifier.padding(top = 8.dp)
        ) { Text("Salir de la app") }
    }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Cerrar sesión") },
            text = { Text("¿Seguro que quieres salir de tu cuenta?") },
            confirmButton = {
                TextButton(onClick = {
                    showLogoutDialog = false
                    onLogout()
                }) { Text("Sí, cerrar sesión") }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) { Text("Cancelar") }
            }
        )
    }
}
