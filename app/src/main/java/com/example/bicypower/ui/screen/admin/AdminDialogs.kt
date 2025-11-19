package com.example.bicypower.ui.screen.admin

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.bicypower.domain.validation.validateEmail
import com.example.bicypower.domain.validation.validateNameLettersOnly
import com.example.bicypower.domain.validation.validatePhoneDigitsOnly
import com.example.bicypower.domain.validation.validatePrice
import com.example.bicypower.domain.validation.validateProductName
import com.example.bicypower.domain.validation.validateRequired
import com.example.bicypower.domain.validation.validateStock
import com.example.bicypower.domain.validation.validateStrongPassword

// ----------------------------------------------------------------------
// DIÁLOGO: NUEVO STAFF
// ----------------------------------------------------------------------
@Composable
fun CreateStaffDialog(
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
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } },
        title = { Text("Nuevo staff") },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        onName(it)
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
                    onValueChange = {
                        onPhone(it)
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

// ----------------------------------------------------------------------
// DIÁLOGO: NUEVO PRODUCTO (ADMIN)
// ----------------------------------------------------------------------
@Composable
fun CreateProductDialog(
    name: String,
    price: String,
    imageUrl: String,
    description: String,
    stock: String,
    onNameChange: (String) -> Unit,
    onPriceChange: (String) -> Unit,
    onImageUrlChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onStockChange: (String) -> Unit,
    onPickFromGallery: () -> Unit,
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

    fun validateProduct(): Boolean {
        nameError = validateProductName(name)
        priceError = validatePrice(price)
        imageError = validateRequired(imageUrl, "La imagen")
        descError = validateRequired(description, "La descripción")
        stockError = validateStock(stock)

        genericError = nameError ?: priceError ?: imageError ?: descError ?: stockError
        return genericError == null
    }

    val messageToShow = error ?: genericError
    val allFilled = name.isNotBlank() && price.isNotBlank() &&
            imageUrl.isNotBlank() && description.isNotBlank() &&
            stock.isNotBlank()

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = {
                    // SOLO entra si TODAS las validaciones pasan
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
            Column {
                // Nombre
                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        onNameChange(it)
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

                // Precio
                OutlinedTextField(
                    value = price,
                    onValueChange = {
                        onPriceChange(it)
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
                    value = imageUrl,
                    onValueChange = {
                        onImageUrlChange(it)
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

                TextButton(onClick = onPickFromGallery) {
                    Text("Elegir desde galería")
                }

                // Descripción
                OutlinedTextField(
                    value = description,
                    onValueChange = {
                        onDescriptionChange(it)
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

                // Stock
                OutlinedTextField(
                    value = stock,
                    onValueChange = {
                        onStockChange(it)
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
