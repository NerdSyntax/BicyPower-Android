package com.example.bicypower.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.bicypower.data.local.database.BicyPowerDatabase
import com.example.bicypower.data.local.storage.UserPreferences            // ✅ import
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

class AuthViewModel(app: Application) : AndroidViewModel(app) {

    private val db = BicyPowerDatabase.getInstance(app)
    private val repo = UserRepository(db.userDao())

    // ✅ Preferencias persistentes (DataStore)
    private val prefs = UserPreferences(app)

    // ---------- LOGIN ----------
    private val _login = MutableStateFlow(LoginUiState())
    val login = _login.asStateFlow()

    fun clearLoginResult() {
        _login.value = _login.value.copy(success = false, errorMsg = null)
    }

    fun onLoginEmailChange(v: String) {
        val e = validateEmail(v)
        _login.value = _login.value.copy(
            email = v,
            emailError = e,
            canSubmit = canLogin(_login.value.copy(email = v, emailError = e))
        )
    }

    fun onLoginPassChange(v: String) {
        val err = if (v.isBlank()) "La contraseña es obligatoria" else null
        _login.value = _login.value.copy(
            pass = v,
            passError = err,
            canSubmit = canLogin(_login.value.copy(pass = v, passError = err))
        )
    }

    private fun canLogin(s: LoginUiState): Boolean =
        s.emailError == null && s.passError == null &&
                s.email.isNotBlank() && s.pass.isNotBlank()

    fun submitLogin() {
        val s = _login.value
        if (!canLogin(s) || s.isSubmitting) return

        _login.value = s.copy(isSubmitting = true, errorMsg = null)
        viewModelScope.launch {
            repo.login(s.email, s.pass)
                .onSuccess { u ->
                    // ✅ Persistir sesión e identidad para Profile/Header/etc.
                    prefs.setSession(true, u.role)
                    prefs.setIdentity(u.name, u.email)

                    _login.value = _login.value.copy(
                        isSubmitting = false,
                        success = true,
                        role = u.role,
                        userName = u.name,
                        userEmail = u.email
                    )
                }
                .onFailure { e ->
                    _login.value = _login.value.copy(
                        isSubmitting = false,
                        errorMsg = e.message ?: "Error de login"
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
        val e = validateNameLettersOnly(v)
        _register.value = _register.value.copy(
            name = v,
            nameError = e,
            canSubmit = canRegister(_register.value.copy(name = v, nameError = e))
        )
    }

    fun onRegEmailChange(v: String) {
        val e = validateEmail(v)
        _register.value = _register.value.copy(
            email = v,
            emailError = e,
            canSubmit = canRegister(_register.value.copy(email = v, emailError = e))
        )
    }

    fun onRegPhoneChange(v: String) {
        val e = validatePhoneDigitsOnly(v)
        _register.value = _register.value.copy(
            phone = v,
            phoneError = e,
            canSubmit = canRegister(_register.value.copy(phone = v, phoneError = e))
        )
    }

    fun onRegPassChange(v: String) {
        val e = validateStrongPassword(v)
        val c = validateConfirm(v, _register.value.confirm)
        _register.value = _register.value.copy(
            pass = v,
            passError = e,
            confirmError = c,
            canSubmit = canRegister(
                _register.value.copy(pass = v, passError = e, confirmError = c)
            )
        )
    }

    fun onRegConfirmChange(v: String) {
        val c = validateConfirm(_register.value.pass, v)
        _register.value = _register.value.copy(
            confirm = v,
            confirmError = c,
            canSubmit = canRegister(_register.value.copy(confirm = v, confirmError = c))
        )
    }

    private fun canRegister(s: RegisterUiState): Boolean =
        s.nameError == null && s.emailError == null && s.phoneError == null &&
                s.passError == null && s.confirmError == null &&
                s.name.isNotBlank() && s.email.isNotBlank() && s.phone.isNotBlank() &&
                s.pass.isNotBlank() && s.confirm.isNotBlank()

    fun submitRegister() {
        val s = _register.value
        if (!canRegister(s) || s.isSubmitting) return

        _register.value = s.copy(isSubmitting = true, errorMsg = null)
        viewModelScope.launch {
            repo.register(s.name, s.email, s.phone, s.pass)
                .onSuccess {
                    _register.value = _register.value.copy(isSubmitting = false, success = true)
                }
                .onFailure { e ->
                    _register.value = _register.value.copy(
                        isSubmitting = false,
                        errorMsg = e.message ?: "Error de registro"
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
        val e = validateEmail(v)
        _forgot.value = _forgot.value.copy(email = v, emailError = e)
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
            val exists = db.userDao().getByEmail(s.email.trim()) != null
            _forgot.value = if (exists) {
                _forgot.value.copy(isSubmitting = false, success = true)
            } else {
                _forgot.value.copy(isSubmitting = false, errorMsg = "Correo no registrado")
            }
        }
    }
}
