package com.example.bicypower.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import com.example.bicypower.data.local.session.UserSession

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTopBar(
    onOpenDrawer: () -> Unit,
    onHome: () -> Unit,
    onLogin: () -> Unit,
    onRegister: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    // ✅ Sesión desde DataStore (UserSession)
    val context = LocalContext.current
    val session = remember { UserSession(context) }
    val isLoggedIn by session.isLoggedIn.collectAsState(initial = false)
    val role by session.role.collectAsState(initial = "")

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
            val active = MaterialTheme.colorScheme.onPrimary
            val inactive = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.4f)
            fun tintFor(r: String) = if (isLoggedIn && role == r) active else inactive

            Icon(Icons.Filled.VerifiedUser, contentDescription = "Admin", tint = tintFor("ADMIN"))
            Icon(Icons.Filled.Badge, contentDescription = "Staff", tint = tintFor("STAFF"))
            Icon(Icons.Filled.Person, contentDescription = "Usuario", tint = tintFor("CLIENT"))

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
