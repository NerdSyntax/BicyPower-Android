package com.example.bicypower.ui.screen

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.bicypower.R
import com.example.bicypower.ui.viewmodel.AuthViewModel
import com.example.bicypower.ui.viewmodel.RegisterUiState

// Colores
private val AzulFondo = Color(0xFF123A6D)
private val FondoClaro = Color(0xFFB5DCF4)
private val BlancoCard = Color(0xFFF9FAFF)
private val RojoError = Color(0xFFD32F2F)

// ----------- VIEWMODEL WRAPPER ----------
@Composable
fun RegisterScreenVm(
    onRegisteredNavigateVerify: (String) -> Unit,
    onGoLogin: () -> Unit
) {
    val vm: AuthViewModel = viewModel()
    val state = vm.register.collectAsStateWithLifecycle().value

    var showSuccessDialog by remember { mutableStateOf(false) }

    LaunchedEffect(state.success) {
        if (state.success) {
            showSuccessDialog = true
        }
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
        onGoLogin = onGoLogin,
        showSuccessDialog = showSuccessDialog,
        onSuccessHandled = {
            // Usamos SIEMPRE el email del estado de registro
            val emailRegistrado = state.email
            showSuccessDialog = false
            vm.clearRegisterResult()
            onRegisteredNavigateVerify(emailRegistrado)
        }
    )
}

// ----------- UI PRINCIPAL ----------
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
    onGoLogin: () -> Unit,
    showSuccessDialog: Boolean,
    onSuccessHandled: () -> Unit
) {
    val focus = LocalFocusManager.current
    var mostrarPass by remember { mutableStateOf(false) }
    var mostrarConfirm by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AzulFondo)
    ) {

        // Fondo diagonal
        DiagonalBackgroundRegister(
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

            Image(
                painter = painterResource(id = R.drawable.logo_bicypower),
                contentDescription = "Logo BicyPower",
                modifier = Modifier.size(52.dp)
            )

            Spacer(Modifier.height(6.dp))

            Text(
                text = "Crear cuenta",
                style = MaterialTheme.typography.headlineMedium,
                color = Color.White
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = "Completa tus datos para registrarte en BicyPower",
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.85f)
            )

            Spacer(Modifier.height(24.dp))

            // ------------ CARD ------------
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                elevation = CardDefaults.cardElevation(18.dp),
                colors = CardDefaults.cardColors(containerColor = BlancoCard)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {

                    // Nombre
                    BicyTextField(
                        value = name,
                        onValueChange = onNameChange,
                        label = "Nombre",
                        isError = nameError != null,
                        modifier = Modifier.fillMaxWidth()
                    )
                    ErrorText(nameError)

                    Spacer(Modifier.height(8.dp))

                    // Email
                    BicyTextField(
                        value = email,
                        onValueChange = onEmailChange,
                        label = "Correo electrónico",
                        isError = emailError != null,
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Email,
                            imeAction = ImeAction.Next
                        )
                    )
                    ErrorText(emailError)

                    Spacer(Modifier.height(8.dp))

                    // Teléfono
                    BicyTextField(
                        value = phone,
                        onValueChange = onPhoneChange,
                        label = "Teléfono",
                        isError = phoneError != null,
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Phone,
                            imeAction = ImeAction.Next
                        )
                    )
                    ErrorText(phoneError)

                    Spacer(Modifier.height(8.dp))

                    // Contraseña
                    BicyPasswordField(
                        value = pass,
                        onValueChange = onPassChange,
                        label = "Contraseña",
                        isError = passError != null,
                        mostrar = mostrarPass,
                        onToggleMostrar = { mostrarPass = !mostrarPass },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
                    )
                    ErrorText(passError)

                    Spacer(Modifier.height(8.dp))

                    // Confirmación
                    BicyPasswordField(
                        value = confirm,
                        onValueChange = onConfirmChange,
                        label = "Confirmar contraseña",
                        isError = confirmError != null,
                        mostrar = mostrarConfirm,
                        onToggleMostrar = { mostrarConfirm = !mostrarConfirm },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = {
                            focus.clearFocus()
                            if (canSubmit && !isSubmitting) onSubmit()
                        })
                    )
                    ErrorText(confirmError)

                    Spacer(Modifier.height(16.dp))

                    // Botón registrar
                    Button(
                        onClick = onSubmit,
                        enabled = canSubmit && !isSubmitting,
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
                            Text("Creando cuenta…")
                        } else {
                            Text("Registrarme")
                        }
                    }

                    ErrorText(errorMsg)
                }
            }

            Spacer(Modifier.height(24.dp))

            TextButton(onClick = onGoLogin) {
                Text("¿Ya tienes cuenta? Inicia sesión", color = Color.White)
            }
        }

        // --------- DIÁLOGO DE BIENVENIDA + CÓDIGO ----------
        if (showSuccessDialog) {
            AlertDialog(
                onDismissRequest = { /* no permitir cerrar tocando afuera */ },
                title = {
                    Text(text = "¡Bienvenido a BicyPower!")
                },
                text = {
                    Text(
                        text = "Tu cuenta ha sido creada con éxito.\n\n" +
                                "Te enviamos un código de verificación al correo:\n$email\n\n" +
                                "Usa ese código para ingresar y activar tu cuenta."
                    )
                },
                confirmButton = {
                    TextButton(onClick = onSuccessHandled) {
                        Text("Ir al login")
                    }
                }
            )
        }
    }
}

// ------ COMPONENTES REUTILIZABLES ------

@Composable
private fun ErrorText(msg: String?) {
    msg?.let {
        Text(it, color = RojoError, style = MaterialTheme.typography.labelSmall)
    }
}

@Composable
private fun BicyTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    isError: Boolean,
    modifier: Modifier = Modifier,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        isError = isError,
        singleLine = true,
        modifier = modifier,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        shape = RoundedCornerShape(12.dp)
    )
}

@Composable
private fun BicyPasswordField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    isError: Boolean,
    mostrar: Boolean,
    onToggleMostrar: () -> Unit,
    modifier: Modifier = Modifier,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        isError = isError,
        singleLine = true,
        modifier = modifier,
        visualTransformation = if (mostrar) VisualTransformation.None else PasswordVisualTransformation(),
        trailingIcon = {
            IconButton(onClick = onToggleMostrar) {
                Icon(
                    imageVector = if (mostrar) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                    contentDescription = null
                )
            }
        },
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        shape = RoundedCornerShape(12.dp)
    )
}

@Composable
private fun DiagonalBackgroundRegister(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val path = Path().apply {
            moveTo(0f, size.height * 0.70f)
            lineTo(size.width, size.height * 0.55f)
            lineTo(size.width, size.height)
            lineTo(0f, size.height)
            close()
        }
        drawPath(path, color = FondoClaro)
    }
}
