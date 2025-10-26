package com.example.bicypower.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.bicypower.ui.viewmodel.AuthViewModel
import com.example.bicypower.ui.viewmodel.LoginUiState
import com.example.bicypower.data.local.storage.UserPreferences
import kotlinx.coroutines.launch

@Composable
fun LoginScreenVm(
    onLoginOk: (String) -> Unit,   // devuelve rol
    onGoRegister: () -> Unit,
    onGoForgot: () -> Unit = {}
) {
    val vm: AuthViewModel = viewModel()
    val state: LoginUiState = vm.login.collectAsStateWithLifecycle().value

    val context = LocalContext.current
    val prefs = remember { UserPreferences(context) }
    val scope = rememberCoroutineScope()

    if (state.success) {
        val role = state.role ?: "USER"
        val name = state.userName ?: ""
        val email = state.userEmail ?: state.email // fallback
        LaunchedEffect(role, name, email) {
            scope.launch {
                // ✅ Persistimos sesión e identidad ANTES de navegar
                prefs.setSession(true, role)
                prefs.setIdentity(name, email)
            }.join()
            vm.clearLoginResult()
            onLoginOk(role)
        }
        return
    }

    // ---------- UI ----------
    val focus = LocalFocusManager.current
    var showPass by remember { mutableStateOf(false) }

    Box(Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
        Card(
            modifier = Modifier.fillMaxWidth(0.95f),
            shape = CardDefaults.shape,
            elevation = CardDefaults.cardElevation(6.dp)
        ) {
            Column(Modifier.padding(20.dp)) {
                Text("BicyPower", style = MaterialTheme.typography.headlineSmall)
                Spacer(Modifier.height(12.dp))

                OutlinedTextField(
                    value = state.email,
                    onValueChange = vm::onLoginEmailChange,
                    label = { Text("Email") },
                    isError = state.emailError != null,
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email,
                        imeAction = ImeAction.Next
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                if (state.emailError != null) {
                    Text(state.emailError!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelSmall)
                }

                Spacer(Modifier.height(10.dp))

                OutlinedTextField(
                    value = state.pass,
                    onValueChange = vm::onLoginPassChange,
                    label = { Text("Contraseña") },
                    isError = state.passError != null,
                    singleLine = true,
                    visualTransformation = if (showPass) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { showPass = !showPass }) {
                            Icon(
                                imageVector = if (showPass) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                                contentDescription = if (showPass) "Ocultar" else "Mostrar"
                            )
                        }
                    },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = {
                        focus.clearFocus()
                        if (state.canSubmit && !state.isSubmitting) vm.submitLogin()
                    }),
                    modifier = Modifier.fillMaxWidth()
                )
                if (state.passError != null) {
                    Text(state.passError!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelSmall)
                }

                Spacer(Modifier.height(16.dp))

                Button(
                    onClick = vm::submitLogin,
                    enabled = state.canSubmit && !state.isSubmitting,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (state.isSubmitting) {
                        CircularProgressIndicator(strokeWidth = 2.dp, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Validando…")
                    } else {
                        Text("Entrar")
                    }
                }

                if (state.errorMsg != null) {
                    Spacer(Modifier.height(8.dp))
                    Text(state.errorMsg!!, color = MaterialTheme.colorScheme.error)
                }

                Spacer(Modifier.height(8.dp))
                TextButton(onClick = onGoForgot, modifier = Modifier.align(Alignment.End)) {
                    Text("¿Olvidaste tu contraseña?")
                }

                Spacer(Modifier.height(8.dp))
                OutlinedButton(onClick = onGoRegister, modifier = Modifier.fillMaxWidth()) {
                    Text("Crear cuenta")
                }
            }
        }
    }
}
