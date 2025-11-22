package com.example.bicypower.ui.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.example.bicypower.data.local.database.BicyPowerDatabase
import com.example.bicypower.data.repository.UserRepository
import com.example.bicypower.domain.validation.validateConfirm
import com.example.bicypower.domain.validation.validateEmail
import com.example.bicypower.domain.validation.validateStrongPassword
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChangePasswordScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val db = remember { BicyPowerDatabase.getInstance(context) }
    val userDao = remember { db.userDao() }
    val repo = remember { UserRepository(userDao) }

    val scope = rememberCoroutineScope()

    var email by remember { mutableStateOf("") }
    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    var emailError by remember { mutableStateOf<String?>(null) }
    var currentPasswordError by remember { mutableStateOf<String?>(null) }
    var newPasswordError by remember { mutableStateOf<String?>(null) }
    var confirmPasswordError by remember { mutableStateOf<String?>(null) }

    var showCurrentPassword by remember { mutableStateOf(false) }
    var showNewPassword by remember { mutableStateOf(false) }
    var showConfirmPassword by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }

    fun showMessage(msg: String) {
        scope.launch { snackbarHostState.showSnackbar(msg) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Cambiar contraseña") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { inner ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(inner)
                .padding(16.dp)
        ) {

            Text("Ingresa tu correo y tu contraseña actual para actualizarla.")

            Spacer(Modifier.height(16.dp))

            // EMAIL
            OutlinedTextField(
                value = email,
                onValueChange = {
                    email = it.trim()
                    emailError = validateEmail(email)
                },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Correo electrónico") },
                singleLine = true,
                isError = emailError != null
            )
            if (emailError != null) {
                Text(
                    text = emailError ?: "",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(Modifier.height(8.dp))

            // CONTRASEÑA ACTUAL
            OutlinedTextField(
                value = currentPassword,
                onValueChange = {
                    currentPassword = it
                    currentPasswordError =
                        if (currentPassword.isBlank()) "La contraseña actual es obligatoria" else null
                },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Contraseña actual") },
                singleLine = true,
                isError = currentPasswordError != null,
                visualTransformation = if (showCurrentPassword)
                    VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { showCurrentPassword = !showCurrentPassword }) {
                        Icon(
                            imageVector = if (showCurrentPassword) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                            contentDescription = if (showCurrentPassword) "Ocultar" else "Mostrar"
                        )
                    }
                }
            )
            if (currentPasswordError != null) {
                Text(
                    text = currentPasswordError ?: "",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(Modifier.height(8.dp))

            // NUEVA CONTRASEÑA
            OutlinedTextField(
                value = newPassword,
                onValueChange = {
                    newPassword = it
                    newPasswordError = validateStrongPassword(newPassword)
                    confirmPasswordError = validateConfirm(newPassword, confirmPassword)
                },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Nueva contraseña") },
                singleLine = true,
                isError = newPasswordError != null,
                visualTransformation = if (showNewPassword)
                    VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { showNewPassword = !showNewPassword }) {
                        Icon(
                            imageVector = if (showNewPassword) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                            contentDescription = if (showNewPassword) "Ocultar" else "Mostrar"
                        )
                    }
                }
            )
            if (newPasswordError != null) {
                Text(
                    text = newPasswordError ?: "",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(Modifier.height(8.dp))

            // CONFIRMAR NUEVA CONTRASEÑA
            OutlinedTextField(
                value = confirmPassword,
                onValueChange = {
                    confirmPassword = it
                    confirmPasswordError = validateConfirm(newPassword, confirmPassword)
                },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Confirmar nueva contraseña") },
                singleLine = true,
                isError = confirmPasswordError != null,
                visualTransformation = if (showConfirmPassword)
                    VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { showConfirmPassword = !showConfirmPassword }) {
                        Icon(
                            imageVector = if (showConfirmPassword) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                            contentDescription = if (showConfirmPassword) "Ocultar" else "Mostrar"
                        )
                    }
                }
            )
            if (confirmPasswordError != null) {
                Text(
                    text = confirmPasswordError ?: "",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(Modifier.height(24.dp))

            Button(
                onClick = {
                    if (isLoading) return@Button

                    scope.launch {
                        emailError = validateEmail(email)
                        currentPasswordError =
                            if (currentPassword.isBlank()) "La contraseña actual es obligatoria" else null
                        newPasswordError = validateStrongPassword(newPassword)
                        confirmPasswordError = validateConfirm(newPassword, confirmPassword)

                        if (emailError != null ||
                            currentPasswordError != null ||
                            newPasswordError != null ||
                            confirmPasswordError != null
                        ) {
                            showMessage("Revisa los campos marcados en rojo.")
                            return@launch
                        }

                        isLoading = true
                        try {
                            val result = repo.changePasswordRemote(
                                email = email,
                                currentPassword = currentPassword,
                                newPassword = newPassword
                            )

                            result.onSuccess {
                                showMessage("Contraseña actualizada con éxito.")
                                onBack()
                            }.onFailure { e ->
                                showMessage(e.message ?: "No se pudo actualizar la contraseña.")
                            }
                        } finally {
                            isLoading = false
                        }
                    }
                },
                enabled = !isLoading,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (isLoading) "Guardando..." else "Guardar cambios")
            }
        }
    }
}
