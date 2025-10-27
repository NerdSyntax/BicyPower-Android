package com.example.bicypower.ui.screen

import android.content.pm.PackageManager
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Rule
import androidx.compose.material3.Button
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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

    // Estado local para mostrar/ocultar confirmación
    var askLogout by remember { mutableStateOf(false) }

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
                ElevatedCard(
                    modifier = Modifier.animateContentSize() // animación suave de la altura
                ) {
                    ListItem(headlineContent = { Text("Cuenta") })
                    HorizontalDivider()

                    // Fila principal con botón "Cerrar sesión"
                    ListItem(
                        leadingContent = { Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null) },
                        headlineContent = { Text("Cerrar sesión") },
                        supportingContent = { Text("Sal de tu cuenta en este dispositivo.") },
                        trailingContent = {
                            Button(onClick = { askLogout = !askLogout }) {
                                Text(if (askLogout) "Cancelar" else "Cerrar sesión")
                            }
                        }
                    )

                    // Banda de confirmación con AnimatedVisibility
                    AnimatedVisibility(
                        visible = askLogout,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        ListItem(
                            leadingContent = { Icon(Icons.Filled.Close, contentDescription = null) },
                            headlineContent = {
                                Text(
                                    "¿Confirmar cierre de sesión?",
                                    style = MaterialTheme.typography.titleSmall
                                )
                            },
                            trailingContent = {
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    OutlinedButton(onClick = { askLogout = false }) {
                                        Icon(Icons.Filled.Close, contentDescription = null)
                                        Text("  No")
                                    }
                                    Button(onClick = onLogout) {
                                        Icon(Icons.Filled.Check, contentDescription = null)
                                        Text("  Sí, salir")
                                    }
                                }
                            }
                        )
                    }
                }
            }

            // ===== Información / Políticas =====
            item {
                ElevatedCard {
                    ListItem(headlineContent = { Text("Información") })
                    HorizontalDivider()
                    ListItem(
                        leadingContent = { Icon(Icons.Filled.Info, contentDescription = null) },
                        headlineContent = { Text("Acerca de") },
                        supportingContent = {
                            Text("BicyPower • versión $versionName", style = MaterialTheme.typography.bodyMedium)
                        }
                    )
                    HorizontalDivider()
                    ListItem(
                        leadingContent = { Icon(Icons.Filled.Rule, contentDescription = null) },
                        headlineContent = { Text("Términos y condiciones") },
                        supportingContent = { Text("Políticas, garantías y devoluciones.") }
                    )
                    Spacer(Modifier.height(8.dp))
                }
            }
        }
    }
}
