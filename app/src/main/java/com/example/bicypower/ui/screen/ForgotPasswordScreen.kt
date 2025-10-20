package com.example.bicypower.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.bicypower.ui.viewmodel.AuthViewModel
import com.example.bicypower.ui.viewmodel.ForgotUiState

@Composable
fun ForgotPasswordScreenVm(
    onEmailSentNavigateLogin: () -> Unit,
    onGoLogin: () -> Unit
) {
    val vm: AuthViewModel = viewModel()
    val state: ForgotUiState = vm.forgot.collectAsStateWithLifecycle().value

    if (state.success) {
        vm.clearForgotResult()
        onEmailSentNavigateLogin()
        return
    }

    ForgotScreen(
        email = state.email,
        emailError = state.emailError,
        isSubmitting = state.isSubmitting,
        errorMsg = state.errorMsg,
        onEmailChange = vm::onForgotEmailChange,
        onSubmit = vm::submitForgot,
        onGoLogin = onGoLogin
    )
}

@Composable
private fun ForgotScreen(
    email: String,
    emailError: String?,
    isSubmitting: Boolean,
    errorMsg: String?,
    onEmailChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onGoLogin: () -> Unit
) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Card(Modifier.fillMaxWidth(0.95f)) {
            Column(Modifier.padding(20.dp)) {
                Text("Recuperar contraseña", style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.height(12.dp))

                OutlinedTextField(
                    value = email,
                    onValueChange = onEmailChange,
                    label = { Text("Email") },
                    singleLine = true,
                    isError = emailError != null,
                    modifier = Modifier.fillMaxWidth()
                )
                if (emailError != null) {
                    Text(emailError, color = MaterialTheme.colorScheme.error)
                }

                Spacer(Modifier.height(16.dp))
                Button(onClick = onSubmit, enabled = !isSubmitting, modifier = Modifier.fillMaxWidth()) {
                    if (isSubmitting) {
                        CircularProgressIndicator(strokeWidth = 2.dp, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Enviando…")
                    } else Text("Enviar enlace")
                }

                if (errorMsg != null) {
                    Spacer(Modifier.height(8.dp))
                    Text(errorMsg, color = MaterialTheme.colorScheme.error)
                }

                Spacer(Modifier.height(8.dp))
                TextButton(onClick = onGoLogin, modifier = Modifier.align(Alignment.End)) { Text("Volver a login") }
            }
        }
    }
}
