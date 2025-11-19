package com.example.bicypower.ui.screen.admin

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.bicypower.data.local.product.ProductEntity
import com.example.bicypower.data.local.user.UserEntity
import com.example.bicypower.domain.validation.validateEmail
import com.example.bicypower.domain.validation.validateNameLettersOnly
import com.example.bicypower.domain.validation.validatePhoneDigitsOnly
import com.example.bicypower.domain.validation.validatePrice
import com.example.bicypower.domain.validation.validateProductName
import com.example.bicypower.domain.validation.validateRequired
import com.example.bicypower.domain.validation.validateStock
import com.example.bicypower.domain.validation.validateStrongPassword
import com.example.bicypower.ui.viewmodel.AdminProductsState
import com.example.bicypower.ui.viewmodel.AdminProductsViewModel
import com.example.bicypower.ui.viewmodel.AdminUsersState
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
            FloatingActionButton(
                onClick = {
                    if (tab == 0) vmUsers.openCreate() else vmProd.openCreate()
                }
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Agregar")
            }
        }
    ) { inner ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(inner)
        ) {
            TabRow(selectedTabIndex = tab) {
                Tab(selected = tab == 0, onClick = { tab = 0 }, text = { Text("Usuarios") })
                Tab(selected = tab == 1, onClick = { tab = 1 }, text = { Text("Productos") })
            }

            if (tab == 0) {
                UsersSection(
                    state = usersState,
                    onDelete = { id -> vmUsers.askDelete(id) }
                )
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
        AdminCreateStaffDialog(
            name = usersState.formName,
            email = usersState.formEmail,
            phone = usersState.formPhone,
            pass = usersState.formPass,
            onName = vmUsers::onFormName,
            onEmail = vmUsers::onFormEmail,
            onPhone = vmUsers::onFormPhone,
            onPass = vmUsers::onFormPass,
            onDismiss = vmUsers::closeCreate,
            onCreate = vmUsers::createStaff,
            isSubmitting = usersState.isSubmitting,
            error = usersState.errorMsg
        )
    }
    if (usersState.confirmDeleteId != null) {
        AlertDialog(
            onDismissRequest = vmUsers::cancelDelete,
            confirmButton = {
                Button(onClick = vmUsers::confirmDelete) { Text("Eliminar") }
            },
            dismissButton = {
                TextButton(onClick = vmUsers::cancelDelete) { Text("Cancelar") }
            },
            title = { Text("Eliminar usuario") },
            text = { Text("¿Seguro que deseas eliminar este usuario?") }
        )
    }

    // ---- Productos ----
    if (prodState.showCreate) {
        AdminCreateProductDialog(
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
            confirmButton = {
                Button(onClick = vmProd::confirmDelete) { Text("Eliminar") }
            },
            dismissButton = {
                TextButton(onClick = vmProd::cancelDelete) { Text("Cancelar") }
            },
            title = { Text("Eliminar producto") },
            text = { Text("¿Seguro que deseas eliminar este producto?") }
        )
    }
}

/* ---------- USUARIOS ---------- */
@Composable
private fun UsersSection(
    state: AdminUsersState,
    onDelete: (Long) -> Unit
) {
    when {
        state.isLoading -> Box(
            Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) { CircularProgressIndicator() }

        state.users.isEmpty() -> Box(
            Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) { Text("No hay usuarios") }

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
        Row(
            Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text(user.name, fontWeight = FontWeight.SemiBold)
                Text(user.email, style = MaterialTheme.typography.labelMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    AssistChip(onClick = {}, label = { Text(user.role) })
                    AssistChip(onClick = {}, label = { Text(user.phone) })
                }
            }
            if (user.role != "ADMIN") {
                IconButton(onClick = onDelete) {
                    Icon(Icons.Filled.Delete, contentDescription = "Eliminar")
                }
            }
        }
    }
}

/* ---------- PRODUCTOS ---------- */
@Composable
private fun ProductsSection(
    state: AdminProductsState,
    onEditPrice: (Long, Double) -> Unit,
    onEditImage: (Long, String) -> Unit,
    onEditStock: (Long, Int) -> Unit,
    onDelete: (Long) -> Unit
) {
    when {
        state.isLoading -> Box(
            Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) { CircularProgressIndicator() }

        state.items.isEmpty() -> Box(
            Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) { Text("No hay productos") }

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
        Row(
            Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = product.imageUrl,
                contentDescription = product.name,
                modifier = Modifier.size(72.dp)
            )
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
                    if (!product.active) {
                        AssistChip(onClick = {}, label = { Text("Inactivo") })
                    }
                }
                if (product.description.isNotBlank()) {
                    Text(
                        product.description,
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }

            Box {
                IconButton(onClick = { menuOpen = true }) {
                    Icon(Icons.Filled.Edit, contentDescription = "Acciones")
                }
                DropdownMenu(
                    expanded = menuOpen,
                    onDismissRequest = { menuOpen = false }
                ) {
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

/* ---------- DIÁLOGO: NUEVO STAFF ---------- */
@Composable
fun AdminCreateStaffDialog(
    name: String,
    email: String,
    phone: String,
    pass: String,
    onName: (String) -> Unit,
    onEmail: (String) -> Unit,
    onPhone: (String) -> Unit,
    onPass: (String) -> Unit,
    onDismiss: () -> Unit,
    onCreate: () -> Unit,
    isSubmitting: Boolean,
    error: String?
) {
    var nameError by remember { mutableStateOf<String?>(null) }
    var emailError by remember { mutableStateOf<String?>(null) }
    var phoneError by remember { mutableStateOf<String?>(null) }
    var passError by remember { mutableStateOf<String?>(null) }
    var genericError by remember { mutableStateOf<String?>(null) }

    fun validateStaff(): Boolean {
        nameError = validateNameLettersOnly(name)
        emailError = validateEmail(email)
        phoneError = validatePhoneDigitsOnly(phone)
        passError = validateStrongPassword(pass)

        genericError = nameError ?: emailError ?: phoneError ?: passError
        return genericError == null
    }

    val messageToShow = error ?: genericError
    val allFilled = name.isNotBlank() && email.isNotBlank() &&
            phone.isNotBlank() && pass.isNotBlank()

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = {
                    if (validateStaff()) {
                        onCreate()
                    }
                },
                enabled = !isSubmitting && allFilled
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(strokeWidth = 2.dp)
                    Spacer(Modifier.width(6.dp))
                }
                Text("Crear staff")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        },
        title = { Text("Nuevo staff") },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { newValue ->
                        // Solo letras y espacios
                        val filtered = newValue.filter { ch ->
                            ch.isLetter() || ch.isWhitespace()
                        }
                        onName(filtered)
                        nameError = null
                        genericError = null
                    },
                    label = { Text("Nombre") },
                    singleLine = true,
                    isError = nameError != null
                )
                if (nameError != null) {
                    Text(
                        nameError!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                OutlinedTextField(
                    value = email,
                    onValueChange = {
                        onEmail(it)
                        emailError = null
                        genericError = null
                    },
                    label = { Text("Email") },
                    singleLine = true,
                    isError = emailError != null
                )
                if (emailError != null) {
                    Text(
                        emailError!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                OutlinedTextField(
                    value = phone,
                    onValueChange = { newValue ->
                        // Solo dígitos
                        val filtered = newValue.filter { ch -> ch.isDigit() }
                        onPhone(filtered)
                        phoneError = null
                        genericError = null
                    },
                    label = { Text("Teléfono") },
                    singleLine = true,
                    isError = phoneError != null,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                if (phoneError != null) {
                    Text(
                        phoneError!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                OutlinedTextField(
                    value = pass,
                    onValueChange = {
                        onPass(it)
                        passError = null
                        genericError = null
                    },
                    label = { Text("Contraseña") },
                    singleLine = true,
                    isError = passError != null
                )
                if (passError != null) {
                    Text(
                        passError!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                if (messageToShow != null) {
                    Spacer(Modifier.width(4.dp))
                    Text(
                        messageToShow,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    )
}

/* ---------- DIÁLOGO: NUEVO PRODUCTO ---------- */
@Composable
private fun AdminCreateProductDialog(
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
    var nameError by remember { mutableStateOf<String?>(null) }
    var priceError by remember { mutableStateOf<String?>(null) }
    var imageError by remember { mutableStateOf<String?>(null) }
    var descError by remember { mutableStateOf<String?>(null) }
    var stockError by remember { mutableStateOf<String?>(null) }
    var genericError by remember { mutableStateOf<String?>(null) }

    val pick = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            onImage(uri.toString())
            imageError = null
            genericError = null
        }
    }

    fun validateProduct(): Boolean {
        nameError = validateProductName(name)
        priceError = validatePrice(price)
        imageError = validateRequired(image, "La imagen")
        descError = validateRequired(desc, "La descripción")
        stockError = validateStock(stock)

        genericError = nameError ?: priceError ?: imageError ?: descError ?: stockError
        return genericError == null
    }

    val messageToShow = error ?: genericError

    // Botón solo habilitado si no hay campos vacíos
    val allFilled = name.isNotBlank() && price.isNotBlank() &&
            image.isNotBlank() && desc.isNotBlank() &&
            stock.isNotBlank()

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = {
                    if (validateProduct()) {
                        onCreate()
                    }
                },
                enabled = !isSubmitting && allFilled
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(strokeWidth = 2.dp)
                    Spacer(Modifier.width(6.dp))
                }
                Text("Crear producto")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        },
        title = { Text("Nuevo producto") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                // Nombre: solo letras y espacios
                OutlinedTextField(
                    value = name,
                    onValueChange = { newValue ->
                        val filtered = newValue.filter { ch ->
                            ch.isLetter() || ch.isWhitespace()
                        }
                        onName(filtered)
                        nameError = null
                        genericError = null
                    },
                    label = { Text("Nombre") },
                    singleLine = true,
                    isError = nameError != null
                )
                if (nameError != null) {
                    Text(
                        nameError!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                // Precio: solo dígitos
                OutlinedTextField(
                    value = price,
                    onValueChange = { newValue ->
                        val filtered = newValue.filter { ch -> ch.isDigit() }
                        onPrice(filtered)
                        priceError = null
                        genericError = null
                    },
                    label = { Text("Precio") },
                    singleLine = true,
                    isError = priceError != null,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                if (priceError != null) {
                    Text(
                        priceError!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                // Imagen
                OutlinedTextField(
                    value = image,
                    onValueChange = {
                        onImage(it)
                        imageError = null
                        genericError = null
                    },
                    label = { Text("URL de imagen o content://") },
                    singleLine = true,
                    isError = imageError != null
                )
                if (imageError != null) {
                    Text(
                        imageError!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                TextButton(onClick = { pick.launch("image/*") }) {
                    Text("Elegir desde galería")
                }

                // Descripción
                OutlinedTextField(
                    value = desc,
                    onValueChange = {
                        onDesc(it)
                        descError = null
                        genericError = null
                    },
                    label = { Text("Descripción") },
                    singleLine = false,
                    isError = descError != null
                )
                if (descError != null) {
                    Text(
                        descError!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                // Stock: solo dígitos
                OutlinedTextField(
                    value = stock,
                    onValueChange = { newValue ->
                        val filtered = newValue.filter { ch -> ch.isDigit() }
                        onStock(filtered)
                        stockError = null
                        genericError = null
                    },
                    label = { Text("Stock inicial") },
                    singleLine = true,
                    isError = stockError != null,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                if (stockError != null) {
                    Text(
                        stockError!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                if (messageToShow != null) {
                    Text(
                        messageToShow,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    )
}

/* ---------- EDITAR PRECIO ---------- */
@Composable
private fun EditPriceDialog(
    price: String,
    onPrice: (String) -> Unit,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = { Button(onClick = onConfirm) { Text("Guardar") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } },
        title = { Text("Editar precio") },
        text = {
            OutlinedTextField(
                value = price,
                onValueChange = { newValue ->
                    val filtered = newValue.filter { ch -> ch.isDigit() }
                    onPrice(filtered)
                },
                label = { Text("Precio") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
        }
    )
}

/* ---------- EDITAR IMAGEN ---------- */
@Composable
private fun EditImageDialog(
    url: String,
    onUrl: (String) -> Unit,
    onClear: () -> Unit,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    val pick = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
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
                    AsyncImage(
                        model = url,
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp)
                    )
                }
                OutlinedTextField(
                    value = url,
                    onValueChange = onUrl,
                    label = { Text("URL / content://") },
                    singleLine = true
                )
            }
        }
    )
}

/* ---------- EDITAR STOCK ---------- */
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
                onValueChange = { newValue ->
                    val filtered = newValue.filter { ch -> ch.isDigit() }
                    onStock(filtered)
                },
                label = { Text("Cantidad") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
        }
    )
}
