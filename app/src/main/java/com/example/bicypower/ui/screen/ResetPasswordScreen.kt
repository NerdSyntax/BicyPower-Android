package com.example.bicypower.ui.screen

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.bicypower.ui.viewmodel.AuthViewModel
import com.example.bicypower.ui.viewmodel.ResetPasswordUiState

private val AzulFondo = Color(0xFF123A6D)
private val BlancoCard = Color(0xFFF9FAFF)
private val FondoClaro = Color(0xFFB5DCF4)
private val RojoError = Color(0xFFD32F2F)

@Composable
fun ResetPasswordScreenVm(
    email: String,
    onResetOkGoLogin: () -> Unit
) {
    val vm: AuthViewModel = viewModel()
    val state: ResetPasswordUiState = vm.reset.collectAsStateWithLifecycle().value

    // inicializa el email la primera vez
    LaunchedEffect(email) {
        vm.initResetEmail(email)
    }

    if (state.success) {
        vm.clearResetResult()
        onResetOkGoLogin()
        return
    }

    ResetPasswordScreen(
        email = state.email,
        code = state.code,
        newPass = state.newPass,
        confirm = state.confirm,
        codeError = state.codeError,
        newPassError = state.newPassError,
        confirmError = state.confirmError,
        isSubmitting = state.isSubmitting,
        errorMsg = state.errorMsg,
        onCodeChange = vm::onResetCodeChange,
        onNewPassChange = vm::onResetPassChange,
        onConfirmChange = vm::onResetConfirmChange,
        onSubmit = vm::submitResetPassword
    )
}

@Composable
private fun ResetPasswordScreen(
    email: String,
    code: String,
    newPass: String,
    confirm: String,
    codeError: String?,
    newPassError: String?,
    confirmError: String?,
    isSubmitting: Boolean,
    errorMsg: String?,
    onCodeChange: (String) -> Unit,
    onNewPassChange: (String) -> Unit,
    onConfirmChange: (String) -> Unit,
    onSubmit: () -> Unit
) {
    var showNew by remember { mutableStateOf(false) }
    var showConfirm by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AzulFondo)
    ) {
        Canvas(
            modifier = Modifier
                .matchParentSize()
                .align(Alignment.BottomCenter)
        ) {
            val path = Path().apply {
                moveTo(0f, size.height * 0.70f)
                lineTo(size.width, size.height * 0.55f)
                lineTo(size.width, size.height)
                lineTo(0f, size.height)
                close()
            }
            drawPath(path, color = FondoClaro)
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 36.dp)
        ) {

            Spacer(Modifier.height(16.dp))

            Text(
                text = "Restablecer contraseña",
                style = MaterialTheme.typography.headlineMedium,
                color = Color.White
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = "Hemos enviado un código a:\n$email",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.9f)
            )

            Spacer(Modifier.height(24.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = BlancoCard),
                elevation = CardDefaults.cardElevation(18.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {

                    OutlinedTextField(
                        value = code,
                        onValueChange = onCodeChange,
                        label = { Text("Código de recuperación") },
                        singleLine = true,
                        isError = codeError != null,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    codeError?.let {
                        Text(it, color = RojoError, style = MaterialTheme.typography.labelSmall)
                    }

                    Spacer(Modifier.height(12.dp))

                    OutlinedTextField(
                        value = newPass,
                        onValueChange = onNewPassChange,
                        label = { Text("Nueva contraseña") },
                        singleLine = true,
                        isError = newPassError != null,
                        modifier = Modifier.fillMaxWidth(),
                        visualTransformation = if (showNew) {
                            VisualTransformation.None
                        } else {
                            PasswordVisualTransformation()
                        },
                        trailingIcon = {
                            IconButton(onClick = { showNew = !showNew }) {
                                Icon(
                                    imageVector = if (showNew) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                                    contentDescription = null
                                )
                            }
                        },
                        shape = RoundedCornerShape(12.dp)
                    )
                    newPassError?.let {
                        Text(it, color = RojoError, style = MaterialTheme.typography.labelSmall)
                    }

                    Spacer(Modifier.height(12.dp))

                    OutlinedTextField(
                        value = confirm,
                        onValueChange = onConfirmChange,
                        label = { Text("Confirmar nueva contraseña") },
                        singleLine = true,
                        isError = confirmError != null,
                        modifier = Modifier.fillMaxWidth(),
                        visualTransformation = if (showConfirm) {
                            VisualTransformation.None
                        } else {
                            PasswordVisualTransformation()
                        },
                        trailingIcon = {
                            IconButton(onClick = { showConfirm = !showConfirm }) {
                                Icon(
                                    imageVector = if (showConfirm) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                                    contentDescription = null
                                )
                            }
                        },
                        shape = RoundedCornerShape(12.dp)
                    )
                    confirmError?.let {
                        Text(it, color = RojoError, style = MaterialTheme.typography.labelSmall)
                    }

                    Spacer(Modifier.height(16.dp))

                    Button(
                        onClick = onSubmit,
                        enabled = !isSubmitting,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(999.dp)
                    ) {
                        if (isSubmitting) {
                            CircularProgressIndicator(
                                strokeWidth = 2.dp,
                                modifier = Modifier.size(18.dp),
                                color = Color.White
                            )
                            Spacer(Modifier.width(8.dp))
                            Text("Guardando…")
                        } else {
                            Text("Restablecer contraseña")
                        }
                    }

                    errorMsg?.let {
                        Spacer(Modifier.height(8.dp))
                        Text(it, color = RojoError, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }
}
