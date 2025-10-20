package com.example.bicypower.ui.screen.admin

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
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
        confirmButton = {
            Button(onClick = onCreate, enabled = !isSubmitting) {
                if (isSubmitting) {
                    CircularProgressIndicator(strokeWidth = 2.dp)
                    Spacer(Modifier.width(6.dp))
                }
                Text("Crear Staff")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } },
        title = { Text("Nuevo Staff") },
        text = {
            Column {
                OutlinedTextField(value = name,  onValueChange = onName,  label = { Text("Nombre") },  singleLine = true)
                OutlinedTextField(value = email, onValueChange = onEmail, label = { Text("Email") },   singleLine = true)
                OutlinedTextField(value = phone, onValueChange = onPhone, label = { Text("Teléfono") },singleLine = true)
                OutlinedTextField(value = pass,  onValueChange = onPass,  label = { Text("Contraseña") }, singleLine = true)
                if (error != null) Text(error, color = MaterialTheme.colorScheme.error)
            }
        }
    )
}
