package com.example.bicypower.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.bicypower.data.local.database.BicyPowerDatabase
import com.example.bicypower.data.local.storage.UserPreferences
import com.example.bicypower.data.repository.UserRepository
import com.example.bicypower.domain.validation.validateConfirm
import com.example.bicypower.domain.validation.validateEmail
import com.example.bicypower.domain.validation.validateNameLettersOnly
import com.example.bicypower.domain.validation.validatePhoneDigitsOnly
import com.example.bicypower.domain.validation.validateStrongPassword
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// ----------------- UI STATES -----------------

data class LoginUiState(
    val email: String = "",
    val pass: String = "",
    val emailError: String? = null,
    val passError: String? = null,
    val canSubmit: Boolean = false,
    val isSubmitting: Boolean = false,
    val errorMsg: String? = null,
    val success: Boolean = false,
    val role: String? = null,
    val userName: String? = null,
    val userEmail: String? = null
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

    val canSubmit: Boolean = false,
    val isSubmitting: Boolean = false,
    val errorMsg: String? = null,
    val success: Boolean = false
)

data class ForgotUiState(
    val email: String = "",
    val emailError: String? = null,
    val isSubmitting: Boolean = false,
    val errorMsg: String? = null,
    val success: Boolean = false
)

data class VerifyUiState(
    val code: String = "",
    val codeError: String? = null,
    val isSubmitting: Boolean = false,
    val errorMsg: String? = null,
    val canSubmit: Boolean = false,
    val success: Boolean = false
)

// ----------------- VIEWMODEL -----------------

class AuthViewModel(app: Application) : AndroidViewModel(app) {

    private val userDao = BicyPowerDatabase.getInstance(app).userDao()
    private val repo = UserRepository(userDao)
    private val prefs = UserPreferences(app)

    // ---------- LOGIN ----------
    private val _login = MutableStateFlow(LoginUiState())
    val login = _login.asStateFlow()

    fun clearLoginResult() {
        _login.value = _login.value.copy(success = false, errorMsg = null)
    }

    fun onLoginEmailChange(v: String) {
        val value = v.trim()
        val e = validateEmail(value)
        val newState = _login.value.copy(
            email = value,
            emailError = e
        )
        _login.value = newState.copy(
            canSubmit = canLogin(newState)
        )
    }

    fun onLoginPassChange(v: String) {
        val value = v
        val err = if (value.isBlank()) "La contraseña es obligatoria" else null
        val newState = _login.value.copy(
            pass = value,
            passError = err
        )
        _login.value = newState.copy(
            canSubmit = canLogin(newState)
        )
    }

    private fun canLogin(s: LoginUiState): Boolean =
        s.emailError == null &&
                s.passError == null &&
                s.email.isNotBlank() &&
                s.pass.isNotBlank()

    fun submitLogin() {
        val s = _login.value
        if (!canLogin(s) || s.isSubmitting) return

        _login.value = s.copy(isSubmitting = true, errorMsg = null)

        viewModelScope.launch {
            val result = repo.loginRemote(s.email, s.pass)

            result.onSuccess { usuario ->
                // guardamos sesión
                prefs.setSession(true, usuario.rol)
                prefs.setIdentity(usuario.nombre, usuario.email)

                _login.value = _login.value.copy(
                    isSubmitting = false,
                    success = true,
                    role = usuario.rol,
                    userName = usuario.nombre,
                    userEmail = usuario.email
                )
            }.onFailure { e ->
                _login.value = _login.value.copy(
                    isSubmitting = false,
                    errorMsg = e.message ?: "Error al iniciar sesión"
                )
            }
        }
    }

    // ---------- REGISTER ----------
    private val _register = MutableStateFlow(RegisterUiState())
    val register = _register.asStateFlow()

    fun clearRegisterResult() {
        _register.value = _register.value.copy(success = false, errorMsg = null)
    }

    fun onRegNameChange(v: String) {
        val value = v.trim()
        val e = validateNameLettersOnly(value)
        val newState = _register.value.copy(
            name = value,
            nameError = e
        )
        _register.value = newState.copy(
            canSubmit = canRegister(newState)
        )
    }

    fun onRegEmailChange(v: String) {
        val value = v.trim()
        val e = validateEmail(value)
        val newState = _register.value.copy(
            email = value,
            emailError = e
        )
        _register.value = newState.copy(
            canSubmit = canRegister(newState)
        )
    }

    fun onRegPhoneChange(v: String) {
        val value = v.trim()
        val e = validatePhoneDigitsOnly(value)
        val newState = _register.value.copy(
            phone = value,
            phoneError = e
        )
        _register.value = newState.copy(
            canSubmit = canRegister(newState)
        )
    }

    fun onRegPassChange(v: String) {
        val value = v
        val e = validateStrongPassword(value)
        val c = validateConfirm(value, _register.value.confirm)
        val newState = _register.value.copy(
            pass = value,
            passError = e,
            confirmError = c
        )
        _register.value = newState.copy(
            canSubmit = canRegister(newState)
        )
    }

    fun onRegConfirmChange(v: String) {
        val value = v
        val c = validateConfirm(_register.value.pass, value)
        val newState = _register.value.copy(
            confirm = value,
            confirmError = c
        )
        _register.value = newState.copy(
            canSubmit = canRegister(newState)
        )
    }

    private fun canRegister(s: RegisterUiState): Boolean =
        s.nameError == null &&
                s.emailError == null &&
                s.phoneError == null &&
                s.passError == null &&
                s.confirmError == null &&
                s.name.isNotBlank() &&
                s.email.isNotBlank() &&
                s.phone.isNotBlank() &&
                s.pass.isNotBlank() &&
                s.confirm.isNotBlank()

    fun submitRegister() {
        val s = _register.value
        if (!canRegister(s) || s.isSubmitting) return

        _register.value = s.copy(isSubmitting = true, errorMsg = null)

        viewModelScope.launch {
            repo.registerRemote(
                s.name,
                s.email,
                s.phone,
                s.pass
            )
                .onSuccess {
                    _register.value = _register.value.copy(
                        isSubmitting = false,
                        success = true
                    )
                }
                .onFailure { e ->
                    _register.value = _register.value.copy(
                        isSubmitting = false,
                        errorMsg = e.message ?: "Error al registrar usuario"
                    )
                }
        }
    }

    // ---------- FORGOT ----------
    private val _forgot = MutableStateFlow(ForgotUiState())
    val forgot = _forgot.asStateFlow()

    fun clearForgotResult() {
        _forgot.value = _forgot.value.copy(success = false, errorMsg = null)
    }

    fun onForgotEmailChange(v: String) {
        val value = v.trim()
        val e = validateEmail(value)
        _forgot.value = _forgot.value.copy(email = value, emailError = e)
    }

    fun submitForgot() {
        val s = _forgot.value
        val e = validateEmail(s.email)
        if (e != null) {
            _forgot.value = s.copy(emailError = e)
            return
        }

        viewModelScope.launch {
            _forgot.value = s.copy(isSubmitting = true, errorMsg = null)
            delay(700)

            val exists = repo.getLocalByEmail(s.email) != null

            _forgot.value = if (exists) {
                _forgot.value.copy(isSubmitting = false, success = true)
            } else {
                _forgot.value.copy(
                    isSubmitting = false,
                    errorMsg = "Correo no registrado"
                )
            }
        }
    }

    // ---------- VERIFY CODE ----------
    private val _verify = MutableStateFlow(VerifyUiState())
    val verify = _verify.asStateFlow()

    fun clearVerifyResult() {
        _verify.value = _verify.value.copy(success = false, errorMsg = null)
    }

    fun onVerifyCodeChange(v: String) {
        val value = v.trim()
        val e = if (value.length < 6) "El código debe tener 6 dígitos" else null
        _verify.value = _verify.value.copy(
            code = value,
            codeError = e,
            canSubmit = e == null && value.isNotBlank()
        )
    }

    fun submitVerify(email: String) {
        val s = _verify.value
        if (!s.canSubmit || s.isSubmitting) return

        _verify.value = s.copy(isSubmitting = true, errorMsg = null)

        viewModelScope.launch {
            repo.verifyCode(email.trim(), s.code)
                .onSuccess {
                    _verify.value = _verify.value.copy(
                        isSubmitting = false,
                        success = true
                    )
                }
                .onFailure { e ->
                    _verify.value = _verify.value.copy(
                        isSubmitting = false,
                        errorMsg = e.message ?: "Código incorrecto"
                    )
                }
        }
    }
}
