package com.example.bicypower.ui.viewmodel

import androidx.lifecycle.ViewModel                       // Base de ViewModel
import androidx.lifecycle.viewModelScope                  // Scope de corrutinas ligado al VM
import kotlinx.coroutines.delay                            // Simulamos tareas async (IO/red)
import kotlinx.coroutines.flow.MutableStateFlow            // Estado observable mutable
import kotlinx.coroutines.flow.StateFlow                   // Exposición inmutable
import kotlinx.coroutines.flow.update                      // Helper para actualizar flows
import kotlinx.coroutines.launch                            // Lanzar corrutinas
import com.example.bicypower.domain.validation.*           // ✅ paquete corregido

// ----------------- ESTADOS DE UI (observable con StateFlow) -----------------

data class LoginUiState(
    val email: String = "",
    val pass: String = "",
    val emailError: String? = null,
    val passError: String? = null,
    val isSubmitting: Boolean = false,
    val canSubmit: Boolean = false,
    val success: Boolean = false,
    val errorMsg: String? = null
)

data class RegisterUiState(
    val name: String = "",
    val email: String = "",
    val phone: String = "",
    val pass: String = "",
    val confirm: String = "",
    val nameError: String? = null,
    val emailError: String? = null,
    val phoneError: String? = null,
    val passError: String? = null,
    val confirmError: String? = null,
    val isSubmitting: Boolean = false,
    val canSubmit: Boolean = false,
    val success: Boolean = false,
    val errorMsg: String? = null
)

// ----------------- COLECCIÓN EN MEMORIA (solo para la demo) -----------------

private data class DemoUser(
    val name: String,
    val email: String,
    val phone: String,
    val pass: String
)

class AuthViewModel : ViewModel() {

    companion object {
        private val USERS = mutableListOf(
            DemoUser(name = "Demo", email = "demo@duoc.cl", phone = "12345678", pass = "Demo123!")
        )
    }

    private val _login = MutableStateFlow(LoginUiState())
    val login: StateFlow<LoginUiState> = _login

    private val _register = MutableStateFlow(RegisterUiState())
    val register: StateFlow<RegisterUiState> = _register

    // ----------------- LOGIN -----------------

    fun onLoginEmailChange(value: String) {
        _login.update { it.copy(email = value, emailError = validateEmail(value)) }
        recomputeLoginCanSubmit()
    }

    fun onLoginPassChange(value: String) {
        _login.update { it.copy(pass = value) }
        recomputeLoginCanSubmit()
    }

    private fun recomputeLoginCanSubmit() {
        val s = _login.value
        val can = s.emailError == null && s.email.isNotBlank() && s.pass.isNotBlank()
        _login.update { it.copy(canSubmit = can) }
    }

    fun submitLogin() {
        val s = _login.value
        if (!s.canSubmit || s.isSubmitting) return
        viewModelScope.launch {
            _login.update { it.copy(isSubmitting = true, errorMsg = null, success = false) }
            delay(500)

            val user = USERS.firstOrNull { it.email.equals(s.email, ignoreCase = true) }
            val ok = user != null && user.pass == s.pass

            _login.update {
                it.copy(
                    isSubmitting = false,
                    success = ok,
                    errorMsg = if (!ok) "Credenciales inválidas" else null
                )
            }
        }
    }

    fun clearLoginResult() {
        _login.update { it.copy(success = false, errorMsg = null) }
    }

    // ----------------- REGISTRO -----------------

    fun onNameChange(value: String) {
        val filtered = value.filter { it.isLetter() || it.isWhitespace() }
        _register.update { it.copy(name = filtered, nameError = validateNameLettersOnly(filtered)) }
        recomputeRegisterCanSubmit()
    }

    fun onRegisterEmailChange(value: String) {
        _register.update { it.copy(email = value, emailError = validateEmail(value)) }
        recomputeRegisterCanSubmit()
    }

    fun onPhoneChange(value: String) {
        val digitsOnly = value.filter { it.isDigit() }
        _register.update { it.copy(phone = digitsOnly, phoneError = validatePhoneDigitsOnly(digitsOnly)) }
        recomputeRegisterCanSubmit()
    }

    fun onRegisterPassChange(value: String) {
        _register.update { it.copy(pass = value, passError = validateStrongPassword(value)) }
        _register.update { it.copy(confirmError = validateConfirm(it.pass, it.confirm)) }
        recomputeRegisterCanSubmit()
    }

    fun onConfirmChange(value: String) {
        _register.update { it.copy(confirm = value, confirmError = validateConfirm(it.pass, value)) }
        recomputeRegisterCanSubmit()
    }

    private fun recomputeRegisterCanSubmit() {
        val s = _register.value
        val noErrors = listOf(s.nameError, s.emailError, s.phoneError, s.passError, s.confirmError).all { it == null }
        val filled = s.name.isNotBlank() && s.email.isNotBlank() && s.phone.isNotBlank() && s.pass.isNotBlank() && s.confirm.isNotBlank()
        _register.update { it.copy(canSubmit = noErrors && filled) }
    }

    fun submitRegister() {
        val s = _register.value
        if (!s.canSubmit || s.isSubmitting) return
        viewModelScope.launch {
            _register.update { it.copy(isSubmitting = true, errorMsg = null, success = false) }
            delay(700)

            val duplicated = USERS.any { it.email.equals(s.email, ignoreCase = true) }
            if (duplicated) {
                _register.update { it.copy(isSubmitting = false, success = false, errorMsg = "El usuario ya existe") }
                return@launch
            }

            USERS.add(DemoUser(name = s.name.trim(), email = s.email.trim(), phone = s.phone.trim(), pass = s.pass))
            _register.update { it.copy(isSubmitting = false, success = true, errorMsg = null) }
        }
    }

    fun clearRegisterResult() {
        _register.update { it.copy(success = false, errorMsg = null) }
    }
}
