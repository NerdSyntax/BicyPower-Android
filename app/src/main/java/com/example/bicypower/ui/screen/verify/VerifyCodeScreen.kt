package com.example.bicypower.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.bicypower.ui.viewmodel.AuthViewModel
import com.example.bicypower.ui.viewmodel.VerifyUiState

private val AzulFondo = Color(0xFF123A6D)
private val BlancoCard = Color(0xFFF9FAFF)
private val RojoError = Color(0xFFD32F2F)

@Composable
fun VerifyCodeScreenVm(
    email: String,
    onVerified: () -> Unit,
    onBackToLogin: () -> Unit
) {
    val vm: AuthViewModel = viewModel()
    val state = vm.verify.collectAsStateWithLifecycle().value

    if (state.success) {
        vm.clearVerifyResult()
        onVerified()
        return
    }

    VerifyCodeScreen(
        email = email,
        state = state,
        onCodeChange = vm::onVerifyCodeChange,
        onSubmit = { vm.submitVerify(email) },
        onBackToLogin = onBackToLogin
    )
}

@Composable
private fun VerifyCodeScreen(
    email: String,
    state: VerifyUiState,
    onCodeChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onBackToLogin: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AzulFondo)
            .padding(24.dp)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.Center),
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = BlancoCard),
            elevation = CardDefaults.cardElevation(12.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Verificar cuenta",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "Ingresa el código que enviamos a:",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = email,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(Modifier.height(16.dp))

                OutlinedTextField(
                    value = state.code,
                    onValueChange = onCodeChange,
                    label = { Text("Código de verificación") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                state.codeError?.let {
                    Text(
                        text = it,
                        color = RojoError,
                        style = MaterialTheme.typography.labelSmall
                    )
                }

                Spacer(Modifier.height(16.dp))

                Button(
                    onClick = onSubmit,
                    enabled = state.canSubmit && !state.isSubmitting,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(999.dp)
                ) {
                    if (state.isSubmitting) {
                        CircularProgressIndicator(
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(18.dp),
                            color = Color.White
                        )
                        Spacer(Modifier.width(8.dp))
                        Text("Verificando…")
                    } else {
                        Text("Confirmar código")
                    }
                }

                state.errorMsg?.let {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = it,
                        color = RojoError,
                        style = MaterialTheme.typography.labelSmall
                    )
                }

                Spacer(Modifier.height(8.dp))

                TextButton(onClick = onBackToLogin) {
                    Text("Volver al login")
                }
            }
        }
    }
}
