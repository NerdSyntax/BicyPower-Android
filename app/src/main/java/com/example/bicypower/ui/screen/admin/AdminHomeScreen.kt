package com.example.bicypower.ui.screen.admin

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.bicypower.data.local.product.ProductEntity
import com.example.bicypower.data.local.user.UserEntity
import com.example.bicypower.ui.components.dialogs.CreateStaffDialog
import com.example.bicypower.ui.viewmodel.AdminProductsViewModel
import com.example.bicypower.ui.viewmodel.AdminUsersViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminHomeScreen(
    onLogout: () -> Unit
) {
    val vmUsers: AdminUsersViewModel = viewModel()
    val vmProd: AdminProductsViewModel = viewModel()

    val usersState by vmUsers.state.collectAsState()
    val prodState  by vmProd.state.collectAsState()

    var tab by remember { mutableStateOf(0) } // 0: Usuarios, 1: Productos

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "  Panel ADMIN",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold
                    )
                },
                actions = {
                    TextButton(onClick = onLogout) {
                        Text("Cerrar sesión")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { if (tab==0) vmUsers.openCreate() else vmProd.openCreate() }) {
                Icon(Icons.Filled.Add, contentDescription = "Agregar")
            }
        }
    ) { inner ->
        Column(Modifier.fillMaxSize().padding(inner)) {
            TabRow(selectedTabIndex = tab) {
                Tab(selected = tab==0, onClick = { tab=0 }, text = { Text("Usuarios") })
                Tab(selected = tab==1, onClick = { tab=1 }, text = { Text("Productos") })
            }

            if (tab == 0) {
                UsersSection(usersState, onDelete = { id -> vmUsers.askDelete(id) })
            } else {
                ProductsSection(
                    state = prodState,
                    onEditPrice = { id, current -> vmProd.openEditPrice(id, current) },
                    onEditImage = { id, url -> vmProd.openEditImage(id, url) },
                    onEditStock = { id, stock -> vmProd.openEditStock(id, stock) },
                    onDelete    = { id -> vmProd.askDelete(id) }
                )
            }

            if (prodState.errorMsg != null) {
                Text(
                    prodState.errorMsg ?: "",
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                )
            }
        }
    }

    // ---- Crear Staff ----
    if (usersState.showCreate) {
        CreateStaffDialog(
            name = usersState.formName, email = usersState.formEmail,
            phone = usersState.formPhone, pass = usersState.formPass,
            onName = vmUsers::onFormName, onEmail = vmUsers::onFormEmail,
            onPhone = vmUsers::onFormPhone, onPass = vmUsers::onFormPass,
            onDismiss = vmUsers::closeCreate, onCreate = vmUsers::createStaff,
            isSubmitting = usersState.isSubmitting, error = usersState.errorMsg
        )
    }
    if (usersState.confirmDeleteId != null) {
        AlertDialog(
            onDismissRequest = vmUsers::cancelDelete,
            confirmButton = { Button(onClick = vmUsers::confirmDelete) { Text("Eliminar") } },
            dismissButton = { TextButton(onClick = vmUsers::cancelDelete) { Text("Cancelar") } },
            title = { Text("Eliminar usuario") },
            text = { Text("¿Seguro que deseas eliminar este usuario?") }
        )
    }

    // ---- Productos ----
    if (prodState.showCreate) {
        CreateProductDialog(
            name  = prodState.pName,
            price = prodState.pPrice,
            image = prodState.pImage,
            desc  = prodState.pDesc,
            stock = prodState.pStock,
            onName  = vmProd::onName,
            onPrice = vmProd::onPrice,
            onImage = vmProd::onImage,
            onDesc  = vmProd::onDesc,
            onStock = vmProd::onStock,
            onDismiss = vmProd::closeCreate,
            onCreate  = vmProd::create,
            isSubmitting = prodState.isSubmitting,
            error        = prodState.errorMsg
        )
    }
    if (prodState.editId != null) {
        EditPriceDialog(
            price = prodState.editPrice,
            onPrice = vmProd::onEditPrice,
            onDismiss = vmProd::closeEdit,
            onConfirm = vmProd::applyEditPrice
        )
    }
    if (prodState.editImageId != null) {
        EditImageDialog(
            url = prodState.editImageUrl,
            onUrl = vmProd::onEditImageUrl,
            onClear = vmProd::clearImage,
            onDismiss = vmProd::closeEditImage,
            onConfirm = vmProd::applyEditImage
        )
    }
    if (prodState.editStockId != null) {
        EditStockDialog(
            stock = prodState.editStock,
            onStock  = vmProd::onEditStock,
            onDismiss = vmProd::closeEditStock,
            onConfirm = vmProd::applyEditStock
        )
    }
    if (prodState.confirmDeleteId != null) {
        AlertDialog(
            onDismissRequest = vmProd::cancelDelete,
            confirmButton = { Button(onClick = vmProd::confirmDelete) { Text("Eliminar") } },
            dismissButton = { TextButton(onClick = vmProd::cancelDelete) { Text("Cancelar") } },
            title = { Text("Eliminar producto") },
            text = { Text("¿Seguro que deseas eliminar este producto?") }
        )
    }
}

/* ---------- USUARIOS ---------- */
@Composable
private fun UsersSection(
    state: com.example.bicypower.ui.viewmodel.AdminUsersState,
    onDelete: (Long) -> Unit
) {
    when {
        state.isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        state.users.isEmpty() -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("No hay usuarios") }
        else -> LazyColumn(
            Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(16.dp)
        ) {
            items(state.users, key = { it.id }) { u ->
                UserRow(u, onDelete = { if (u.role != "ADMIN") onDelete(u.id) })
            }
        }
    }
}

@Composable
private fun UserRow(user: UserEntity, onDelete: () -> Unit) {
    ElevatedCard {
        Row(Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(user.name, fontWeight = FontWeight.SemiBold)
                Text(user.email, style = MaterialTheme.typography.labelMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    AssistChip(onClick = {}, label = { Text(user.role) })
                    AssistChip(onClick = {}, label = { Text(user.phone) })
                }
            }
            if (user.role != "ADMIN") {
                IconButton(onClick = onDelete) { Icon(Icons.Filled.Delete, contentDescription = "Eliminar") }
            }
        }
    }
}

/* ---------- PRODUCTOS ---------- */
@Composable
private fun ProductsSection(
    state: com.example.bicypower.ui.viewmodel.AdminProductsState,
    onEditPrice: (Long, Double) -> Unit,
    onEditImage: (Long, String) -> Unit,
    onEditStock: (Long, Int) -> Unit,
    onDelete: (Long) -> Unit
) {
    when {
        state.isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        state.items.isEmpty() -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("No hay productos") }
        else -> LazyColumn(
            Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(16.dp)
        ) {
            items(state.items, key = { it.id }) { p ->
                ProductRow(
                    product = p,
                    onEditPrice = { onEditPrice(p.id, p.price) },
                    onEditImage = { onEditImage(p.id, p.imageUrl) },
                    onEditStock = { onEditStock(p.id, p.stock) },
                    onDelete    = { onDelete(p.id) }
                )
            }
        }
    }
}

@Composable
private fun ProductRow(
    product: ProductEntity,
    onEditPrice: () -> Unit,
    onEditImage: () -> Unit,
    onEditStock: () -> Unit,
    onDelete: () -> Unit
) {
    var menuOpen by remember { mutableStateOf(false) }

    ElevatedCard {
        Row(Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            AsyncImage(model = product.imageUrl, contentDescription = product.name, modifier = Modifier.size(72.dp))
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(product.name, fontWeight = FontWeight.SemiBold)
                Text(
                    "$ ${"%,.0f".format(product.price)}",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    AssistChip(onClick = {}, label = { Text("Stock: ${product.stock}") })
                    if (!product.active) AssistChip(onClick = {}, label = { Text("Inactivo") })
                }
                if (product.description.isNotBlank()) {
                    Text(product.description, style = MaterialTheme.typography.labelMedium)
                }
            }

            Box {
                IconButton(onClick = { menuOpen = true }) {
                    Icon(Icons.Filled.Edit, contentDescription = "Acciones")
                }
                DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                    DropdownMenuItem(
                        text = { Text("Editar precio") },
                        onClick = { menuOpen = false; onEditPrice() }
                    )
                    DropdownMenuItem(
                        text = { Text("Editar imagen (galería)") },
                        onClick = { menuOpen = false; onEditImage() }
                    )
                    DropdownMenuItem(
                        text = { Text("Editar stock") },
                        onClick = { menuOpen = false; onEditStock() }
                    )
                    Divider()
                    DropdownMenuItem(
                        text = { Text("Eliminar") },
                        onClick = { menuOpen = false; onDelete() }
                    )
                }
            }
        }
    }
}

/* ---------- DIÁLOGOS (PRODUCTOS) ---------- */
@Composable
private fun CreateProductDialog(
    name: String,
    price: String,
    image: String,
    desc: String,
    stock: String,
    onName: (String) -> Unit,
    onPrice: (String) -> Unit,
    onImage: (String) -> Unit,
    onDesc: (String) -> Unit,
    onStock: (String) -> Unit,
    onDismiss: () -> Unit,
    onCreate: () -> Unit,
    isSubmitting: Boolean,
    error: String?
) {
    val pick = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) onImage(uri.toString())
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = { Button(onClick = onCreate, enabled = !isSubmitting) { Text("Crear producto") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } },
        title = { Text("Nuevo producto") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = name,  onValueChange = onName,  label = { Text("Nombre") }, singleLine = true)
                OutlinedTextField(value = price, onValueChange = onPrice, label = { Text("Precio") }, singleLine = true)
                OutlinedTextField(value = image, onValueChange = onImage, label = { Text("URL de imagen o content://") }, singleLine = true)
                TextButton(onClick = { pick.launch("image/*") }) { Text("Elegir desde galería") }
                OutlinedTextField(value = desc,  onValueChange = onDesc,  label = { Text("Descripción") })
                OutlinedTextField(value = stock, onValueChange = onStock, label = { Text("Stock inicial") }, singleLine = true)
                if (error != null) Text(error, color = MaterialTheme.colorScheme.error)
            }
        }
    )
}

@Composable
private fun EditPriceDialog(
    price: String, onPrice: (String) -> Unit,
    onDismiss: () -> Unit, onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = { Button(onClick = onConfirm) { Text("Guardar") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } },
        title = { Text("Editar precio") },
        text = {
            OutlinedTextField(
                value = price, onValueChange = onPrice, label = { Text("Precio") },
                singleLine = true, modifier = Modifier.fillMaxWidth()
            )
        }
    )
}

@Composable
private fun EditImageDialog(
    url: String,
    onUrl: (String) -> Unit,
    onClear: () -> Unit,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    val pick = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) onUrl(uri.toString())
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = { Button(onClick = onConfirm) { Text("Guardar") } },
        dismissButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(onClick = { pick.launch("image/*") }) { Text("Elegir imagen") }
                TextButton(onClick = onClear) { Text("Quitar imagen") }
                TextButton(onClick = onDismiss) { Text("Cancelar") }
            }
        },
        title = { Text("Editar imagen") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (url.isNotBlank()) {
                    AsyncImage(model = url, contentDescription = null, modifier = Modifier.fillMaxWidth().height(140.dp))
                }
                OutlinedTextField(value = url, onValueChange = onUrl, label = { Text("URL / content://") }, singleLine = true)
            }
        }
    )
}

@Composable
private fun EditStockDialog(
    stock: String,
    onStock: (String) -> Unit,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = { Button(onClick = onConfirm) { Text("Guardar") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } },
        title = { Text("Editar stock") },
        text = {
            OutlinedTextField(
                value = stock,
                onValueChange = onStock,
                label = { Text("Cantidad") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        }
    )
}
