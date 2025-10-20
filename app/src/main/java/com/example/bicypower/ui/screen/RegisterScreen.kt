package com.example.bicypower.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.bicypower.ui.viewmodel.AuthViewModel
import com.example.bicypower.ui.viewmodel.RegisterUiState

@Composable
fun RegisterScreenVm(
    onRegisteredNavigateLogin: () -> Unit,
    onGoLogin: () -> Unit
) {
    val vm: AuthViewModel = viewModel()
    val state: RegisterUiState = vm.register.collectAsStateWithLifecycle().value

    if (state.success) {
        vm.clearRegisterResult()
        onRegisteredNavigateLogin()
        return
    }

    RegisterScreen(
        name = state.name,
        email = state.email,
        phone = state.phone,
        pass = state.pass,
        confirm = state.confirm,
        nameError = state.nameError,
        emailError = state.emailError,
        phoneError = state.phoneError,
        passError = state.passError,
        confirmError = state.confirmError,
        canSubmit = state.canSubmit,
        isSubmitting = state.isSubmitting,
        errorMsg = state.errorMsg,
        onNameChange = vm::onRegNameChange,
        onEmailChange = vm::onRegEmailChange,
        onPhoneChange = vm::onRegPhoneChange,
        onPassChange = vm::onRegPassChange,
        onConfirmChange = vm::onRegConfirmChange,
        onSubmit = vm::submitRegister,
        onGoLogin = onGoLogin
    )
}

@Composable
private fun RegisterScreen(
    name: String,
    email: String,
    phone: String,
    pass: String,
    confirm: String,
    nameError: String?,
    emailError: String?,
    phoneError: String?,
    passError: String?,
    confirmError: String?,
    canSubmit: Boolean,
    isSubmitting: Boolean,
    errorMsg: String?,
    onNameChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onPhoneChange: (String) -> Unit,
    onPassChange: (String) -> Unit,
    onConfirmChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onGoLogin: () -> Unit
) {
    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("Crear cuenta", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(12.dp))

        OutlinedTextField(value = name, onValueChange = onNameChange, label = { Text("Nombre") }, isError = nameError != null, singleLine = true, modifier = Modifier.fillMaxWidth())
        if (nameError != null) Text(nameError, color = MaterialTheme.colorScheme.error)

        Spacer(Modifier.height(8.dp))
        OutlinedTextField(value = email, onValueChange = onEmailChange, label = { Text("Email") }, isError = emailError != null, singleLine = true, modifier = Modifier.fillMaxWidth())
        if (emailError != null) Text(emailError, color = MaterialTheme.colorScheme.error)

        Spacer(Modifier.height(8.dp))
        OutlinedTextField(value = phone, onValueChange = onPhoneChange, label = { Text("Teléfono") }, isError = phoneError != null, singleLine = true, modifier = Modifier.fillMaxWidth())
        if (phoneError != null) Text(phoneError, color = MaterialTheme.colorScheme.error)

        Spacer(Modifier.height(8.dp))
        OutlinedTextField(value = pass, onValueChange = onPassChange, label = { Text("Contraseña") }, isError = passError != null, singleLine = true, modifier = Modifier.fillMaxWidth())
        if (passError != null) Text(passError, color = MaterialTheme.colorScheme.error)

        Spacer(Modifier.height(8.dp))
        OutlinedTextField(value = confirm, onValueChange = onConfirmChange, label = { Text("Confirmar") }, isError = confirmError != null, singleLine = true, modifier = Modifier.fillMaxWidth())
        if (confirmError != null) Text(confirmError, color = MaterialTheme.colorScheme.error)

        Spacer(Modifier.height(16.dp))
        Button(onClick = onSubmit, enabled = canSubmit && !isSubmitting, modifier = Modifier.fillMaxWidth()) {
            if (isSubmitting) {
                CircularProgressIndicator(strokeWidth = 2.dp, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Creando…")
            } else Text("Registrar")
        }

        if (errorMsg != null) {
            Spacer(Modifier.height(8.dp))
            Text(errorMsg, color = MaterialTheme.colorScheme.error)
        }

        Spacer(Modifier.height(8.dp))
        TextButton(onClick = onGoLogin) { Text("¿Ya tienes cuenta? Inicia sesión") }
    }
}
