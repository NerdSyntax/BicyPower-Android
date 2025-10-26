package com.example.bicypower.ui.components

import androidx.compose.material.icons.Icons // Conjunto de íconos Material
import androidx.compose.material.icons.filled.Home // Ícono Home
import androidx.compose.material.icons.filled.AccountCircle // Ícono Login
import androidx.compose.material.icons.filled.Menu // Ícono hamburguesa
import androidx.compose.material.icons.filled.MoreVert // Ícono 3 puntitos (overflow)
import androidx.compose.material.icons.filled.Person // Ícono Registro
import androidx.compose.material.icons.filled.VerifiedUser // Admin
import androidx.compose.material.icons.filled.Badge // Staff
import androidx.compose.material3.CenterAlignedTopAppBar // TopAppBar centrada
import androidx.compose.material3.DropdownMenu // Menú desplegable
import androidx.compose.material3.DropdownMenuItem // Opción del menú
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon // Para mostrar íconos
import androidx.compose.material3.IconButton // Botones con ícono
import androidx.compose.material3.MaterialTheme // Tema Material
import androidx.compose.material3.Text // Texto
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.* // remember / mutableStateOf
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import com.example.bicypower.data.local.storage.UserPreferences

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTopBar(
    onOpenDrawer: () -> Unit, // Abre el drawer (hamburguesa)
    onHome: () -> Unit,       // Navega a Home
    onLogin: () -> Unit,      // Navega a Login
    onRegister: () -> Unit    // Navega a Registro
) {
    var showMenu by remember { mutableStateOf(false) } // Estado del menú overflow

    // NUEVO: estado de sesión para pintar los 3 íconos de rol
    val context = LocalContext.current
    val prefs = remember { UserPreferences(context) }
    val isLoggedIn by prefs.isLoggedIn.collectAsState(initial = false)
    val role by prefs.role.collectAsState(initial = "")

    CenterAlignedTopAppBar(
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = MaterialTheme.colorScheme.primary
        ),
        title = {
            Text(
                text = "BicyPower",
                style = MaterialTheme.typography.titleLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        navigationIcon = {
            IconButton(onClick = onOpenDrawer) {
                Icon(imageVector = Icons.Filled.Menu, contentDescription = "Menú")
            }
        },
        actions = {
            // --------- NUEVO: 3 íconos de rol con color activo/inactivo ---------
            val active = MaterialTheme.colorScheme.onPrimary
            val inactive = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.4f)
            fun tintFor(r: String) = if (isLoggedIn && role == r) active else inactive

            Icon(Icons.Filled.VerifiedUser, contentDescription = "Admin", tint = tintFor("ADMIN"))
            Icon(Icons.Filled.Badge,       contentDescription = "Staff", tint = tintFor("STAFF"))
            Icon(Icons.Filled.Person,      contentDescription = "Usuario", tint = tintFor("CLIENT"))
            // --------------------------------------------------------------------

            IconButton(onClick = onHome) {
                Icon(Icons.Filled.Home, contentDescription = "Home")
            }
            IconButton(onClick = onLogin) {
                Icon(Icons.Filled.AccountCircle, contentDescription = "Login")
            }
            IconButton(onClick = onRegister) {
                Icon(Icons.Filled.Person, contentDescription = "Registro")
            }
            IconButton(onClick = { showMenu = true }) {
                Icon(Icons.Filled.MoreVert, contentDescription = "Más")
            }
            DropdownMenu(
                expanded = showMenu,
                onDismissRequest = { showMenu = false }
            ) {
                DropdownMenuItem(
                    text = { Text("Home") },
                    onClick = { showMenu = false; onHome() }
                )
                DropdownMenuItem(
                    text = { Text("Login") },
                    onClick = { showMenu = false; onLogin() }
                )
                DropdownMenuItem(
                    text = { Text("Registro") },
                    onClick = { showMenu = false; onRegister() }
                )
            }
        }
    )
}
