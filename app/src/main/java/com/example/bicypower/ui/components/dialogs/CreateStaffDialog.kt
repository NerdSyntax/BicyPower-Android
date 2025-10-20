package com.example.bicypower.ui.components.dialogs

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp

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
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nuevo Staff") },
        text = {
            Column {
                OutlinedTextField(
                    value = name, onValueChange = onName, label = { Text("Nombre") },
                    singleLine = true, enabled = !isSubmitting
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = email, onValueChange = onEmail, label = { Text("Email") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    enabled = !isSubmitting
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = phone, onValueChange = onPhone, label = { Text("Teléfono") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    enabled = !isSubmitting
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = pass, onValueChange = onPass, label = { Text("Contraseñaaa") },
                    singleLine = true,
                    visualTransformation = if (pass.isNotEmpty()) PasswordVisualTransformation() else VisualTransformation.None,
                    enabled = !isSubmitting
                )
                if (!error.isNullOrBlank()) {
                    Spacer(Modifier.height(8.dp))
                    Text(error, color = MaterialTheme.colorScheme.error)
                }
            }
        },
        confirmButton = { Button(onClick = onCreate, enabled = !isSubmitting) { Text(if (isSubmitting) "Creando..." else "Crear") } },
        dismissButton = { TextButton(onClick = onDismiss, enabled = !isSubmitting) { Text("Cancelar") } }
    )
}
