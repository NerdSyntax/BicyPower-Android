package com.example.bicypower.ui.screen

import android.content.pm.PackageManager
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Rule
import androidx.compose.material3.Button
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    val versionName = try {
        val pm: PackageManager = context.packageManager
        val pkg = pm.getPackageInfo(context.packageName, 0)
        pkg.versionName ?: "-"
    } catch (_: Exception) { "-" }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Ajustes") })
        }
    ) { inner ->
        LazyColumn(
            contentPadding = PaddingValues(
                top = inner.calculateTopPadding(),
                start = 16.dp,
                end = 16.dp,
                bottom = 24.dp
            ),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // ===== Cuenta =====
            item {
                ElevatedCard {
                    ListItem(headlineContent = { Text("Cuenta") })
                    HorizontalDivider()
                    ListItem(
                        leadingContent = { Icon(Icons.AutoMirrored.Filled.Logout, null) },
                        headlineContent = { Text("Cerrar sesión") },
                        supportingContent = { Text("Sal de tu cuenta en este dispositivo.") },
                        trailingContent = { Button(onClick = onLogout) { Text("Cerrar sesión") } }
                    )
                }
            }

            // ===== Información / Políticas =====
            item {
                ElevatedCard {
                    ListItem(headlineContent = { Text("Información") })
                    HorizontalDivider()
                    ListItem(
                        leadingContent = { Icon(Icons.Filled.Info, null) },
                        headlineContent = { Text("Acerca de") },
                        supportingContent = {
                            Text("BicyPower • versión $versionName", style = MaterialTheme.typography.bodyMedium)
                        }
                    )
                    HorizontalDivider()
                    ListItem(
                        leadingContent = { Icon(Icons.Filled.Rule, null) },
                        headlineContent = { Text("Términos y condiciones") },
                        supportingContent = { Text("Políticas, garantías y devoluciones.") }
                    )
                    Spacer(Modifier.height(8.dp))
                }
            }
        }
    }
}
