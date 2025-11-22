package com.example.bicypower.ui.screen

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.bicypower.data.local.storage.UserPreferences
import com.example.bicypower.ui.viewmodel.AuthViewModel
import com.example.bicypower.ui.viewmodel.LoginUiState
import kotlinx.coroutines.launch
import com.example.bicypower.R

// Colores base
private val AzulFondo = Color(0xFF123A6D)
private val GrisSuave = Color(0xFFE8ECF5)
private val BlancoCard = Color(0xFFF9FAFF)
private val RojoError = Color(0xFFD32F2F)

@Composable
fun LoginScreenModern(
    onLoginOk: (String) -> Unit,
    onGoRegister: () -> Unit,
    onGoForgot: () -> Unit = {},
    onGoVerifyCode: (String) -> Unit = {}   // 👈 NUEVO PARÁMETRO
) {
    val vm: AuthViewModel = viewModel()
    val state: LoginUiState = vm.login.collectAsStateWithLifecycle().value

    val context = LocalContext.current
    val prefs = remember { UserPreferences(context) }
    val scope = rememberCoroutineScope()
    val focus = LocalFocusManager.current

    var mostrarPass by remember { mutableStateOf(false) }

    // --- Navegación cuando login OK ---
    if (state.success) {
        LaunchedEffect(true) {
            scope.launch {
                prefs.setSession(true, state.role ?: "USER")
                prefs.setIdentity(
                    state.userName ?: "",
                    state.userEmail ?: state.email
                )
            }.join()
            vm.clearLoginResult()
            onLoginOk(state.role ?: "USER")
        }
        return
    }

    // --- UI ---
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AzulFondo)
    ) {

        // Parte inferior suave en diagonal
        DiagonalBackground(
            modifier = Modifier
                .matchParentSize()
                .align(Alignment.BottomCenter)
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 36.dp)
        ) {

            Spacer(Modifier.height(8.dp))

            // Logo BicyPower
            Image(
                painter = painterResource(id = R.drawable.logo_bicypower),
                contentDescription = "Logo BicyPower",
                modifier = Modifier.size(100.dp)
            )

            Spacer(Modifier.height(6.dp))

            Text(
                text = "LOGIN",
                style = MaterialTheme.typography.headlineMedium,
                color = Color.White
            )

            Spacer(Modifier.height(24.dp))

            // -------- CARD DEL FORM ----------
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                elevation = CardDefaults.cardElevation(18.dp),
                colors = CardDefaults.cardColors(containerColor = BlancoCard)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {

                    // CORREO
                    Text(
                        text = "Correo electrónico",
                        style = MaterialTheme.typography.labelMedium,
                        color = AzulFondo
                    )
                    OutlinedTextField(
                        value = state.email,
                        onValueChange = vm::onLoginEmailChange,
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        isError = state.emailError != null,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Email,
                            imeAction = ImeAction.Next
                        )
                    )
                    state.emailError?.let {
                        Text(
                            it,
                            color = RojoError,
                            style = MaterialTheme.typography.labelSmall
                        )
                    }

                    Spacer(Modifier.height(12.dp))

                    // CONTRASEÑA
                    Text(
                        text = "Contraseña",
                        style = MaterialTheme.typography.labelMedium,
                        color = AzulFondo
                    )
                    OutlinedTextField(
                        value = state.pass,
                        onValueChange = vm::onLoginPassChange,
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        isError = state.passError != null,
                        visualTransformation = if (mostrarPass)
                            VisualTransformation.None
                        else
                            PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { mostrarPass = !mostrarPass }) {
                                Icon(
                                    imageVector = if (mostrarPass)
                                        Icons.Filled.VisibilityOff
                                    else
                                        Icons.Filled.Visibility,
                                    contentDescription = if (mostrarPass)
                                        "Ocultar contraseña"
                                    else
                                        "Mostrar contraseña"
                                )
                            }
                        },
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = {
                            focus.clearFocus()
                            if (state.canSubmit && !state.isSubmitting) vm.submitLogin()
                        })
                    )
                    state.passError?.let {
                        Text(
                            it,
                            color = RojoError,
                            style = MaterialTheme.typography.labelSmall
                        )
                    }

                    Spacer(Modifier.height(8.dp))

                    // -------- FILA: Olvidaste / Crear cuenta ----------
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(onClick = onGoForgot) {
                            Text(
                                "¿Olvidaste tu contraseña?",
                                color = AzulFondo
                            )
                        }

                        TextButton(onClick = onGoRegister) {
                            Icon(
                                imageVector = Icons.Filled.Person,
                                contentDescription = "Crear cuenta",
                                tint = AzulFondo
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = "Crear cuenta",
                                color = AzulFondo
                            )
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    // BOTÓN ENTRAR
                    Button(
                        onClick = vm::submitLogin,
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
                            Text("Validando…")
                        } else {
                            Text("Entrar")
                        }
                    }

                    state.errorMsg?.let {
                        Spacer(Modifier.height(8.dp))
                        Text(
                            it,
                            color = RojoError,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    Spacer(Modifier.height(8.dp))

                    // 👇 NUEVO: enlace para ir a ingresar el código
                    TextButton(
                        onClick = {
                            // Usamos el correo escrito arriba; si está vacío igual se puede
                            // mostrar la pantalla y ahí ingresarlo.
                            onGoVerifyCode(state.email)
                        },
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    ) {
                        Text(
                            text = "¿Ya tienes tu código de verificación? Ingrésalo aquí",
                            color = AzulFondo,
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                }
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
private fun DiagonalBackground(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val path = Path().apply {
            moveTo(0f, size.height * 0.70f)
            lineTo(size.width, size.height * 0.55f)
            lineTo(size.width, size.height)
            lineTo(0f, size.height)
            close()
        }

        drawPath(
            path = path,
            color = Color(0xFFDEE6F1) // color suave que combina con azul
        )
    }
}
