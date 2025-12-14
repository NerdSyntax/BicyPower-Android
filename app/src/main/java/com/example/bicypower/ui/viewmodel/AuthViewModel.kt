package com.example.bicypower.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.bicypower.data.local.session.UserSession
import com.example.bicypower.data.repository.UserRepository
import com.example.bicypower.domain.validation.*
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
    val userId: Long? = null,
    val userName: String? = null,
    val userEmail: String? = null,
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

data class ResetPasswordUiState(
    val email: String = "",
    val code: String = "",
    val newPass: String = "",
    val confirm: String = "",
    val codeError: String? = null,
    val newPassError: String? = null,
    val confirmError: String? = null,
    val isSubmitting: Boolean = false,
    val errorMsg: String? = null,
    val success: Boolean = false
)

// ----------------- VIEWMODEL -----------------

class AuthViewModel(app: Application) : AndroidViewModel(app) {

    private val repo = UserRepository()              // ✅ ya no dao
    private val session = UserSession(app)           // ✅ un solo storage

    // ---------- LOGIN ----------
    private val _login = MutableStateFlow(LoginUiState())
    val login = _login.asStateFlow()

    fun clearLoginResult() {
        _login.value = _login.value.copy(success = false, errorMsg = null)
    }

    fun onLoginEmailChange(v: String) {
        val e = validateEmail(v)
        val newState = _login.value.copy(email = v, emailError = e)
        _login.value = newState.copy(canSubmit = canLogin(newState))
    }

    fun onLoginPassChange(v: String) {
        val err = if (v.isBlank()) "La contraseña es obligatoria" else null
        val newState = _login.value.copy(pass = v, passError = err)
        _login.value = newState.copy(canSubmit = canLogin(newState))
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
            repo.loginRemote(s.email, s.pass)
                .onSuccess { usuario ->

                    val idLong = usuario.id?.toLong() ?: 0L

                    session.setSession(
                        role = usuario.rol ?: "CLIENT",
                        userId = idLong,
                        name = usuario.nombre ?: "",
                        email = usuario.email ?: s.email
                    )

                    _login.value = _login.value.copy(
                        isSubmitting = false,
                        success = true,
                        role = usuario.rol,
                        userId = idLong,
                        userName = usuario.nombre,
                        userEmail = usuario.email
                    )
                }
                .onFailure { e ->
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
        val e = validateNameLettersOnly(v)
        val newState = _register.value.copy(name = v, nameError = e)
        _register.value = newState.copy(canSubmit = canRegister(newState))
    }

    fun onRegEmailChange(v: String) {
        val e = validateEmail(v)
        val newState = _register.value.copy(email = v, emailError = e)
        _register.value = newState.copy(canSubmit = canRegister(newState))
    }

    fun onRegPhoneChange(v: String) {
        val e = validatePhoneDigitsOnly(v)
        val newState = _register.value.copy(phone = v, phoneError = e)
        _register.value = newState.copy(canSubmit = canRegister(newState))
    }

    fun onRegPassChange(v: String) {
        val e = validateStrongPassword(v)
        val c = validateConfirm(v, _register.value.confirm)
        val newState = _register.value.copy(pass = v, passError = e, confirmError = c)
        _register.value = newState.copy(canSubmit = canRegister(newState))
    }

    fun onRegConfirmChange(v: String) {
        val c = validateConfirm(_register.value.pass, v)
        val newState = _register.value.copy(confirm = v, confirmError = c)
        _register.value = newState.copy(canSubmit = canRegister(newState))
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
            repo.registerRemote(s.name, s.email, s.phone, s.pass)
                .onSuccess {
                    _register.value = _register.value.copy(isSubmitting = false, success = true)
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

            repo.forgotPasswordRemote(s.email)
                .onSuccess {
                    _forgot.value = _forgot.value.copy(isSubmitting = false, success = true)
                }
                .onFailure { ex ->
                    _forgot.value = _forgot.value.copy(
                        isSubmitting = false,
                        errorMsg = ex.message ?: "No se pudo enviar el correo"
                    )
                }
        }
    }

    // ---------- VERIFY ----------
    private val _verify = MutableStateFlow(VerifyUiState())
    val verify = _verify.asStateFlow()

    fun clearVerifyResult() {
        _verify.value = _verify.value.copy(success = false, errorMsg = null)
    }

    fun onVerifyCodeChange(v: String) {
        val e = if (v.length < 4) "Código demasiado corto" else null
        _verify.value = _verify.value.copy(
            code = v,
            codeError = e,
            canSubmit = (e == null && v.isNotBlank())
        )
    }

    fun submitVerify(email: String) {
        val s = _verify.value
        if (!s.canSubmit || s.isSubmitting) return

        _verify.value = s.copy(isSubmitting = true, errorMsg = null)

        viewModelScope.launch {
            repo.verifyCode(email, s.code)
                .onSuccess {
                    _verify.value = _verify.value.copy(isSubmitting = false, success = true)
                }
                .onFailure { e ->
                    _verify.value = _verify.value.copy(
                        isSubmitting = false,
                        errorMsg = e.message ?: "Código incorrecto"
                    )
                }
        }
    }

    // ---------- RESET ----------
    private val _reset = MutableStateFlow(ResetPasswordUiState())
    val reset = _reset.asStateFlow()

    fun initResetEmail(email: String) {
        _reset.value = _reset.value.copy(email = email)
    }

    fun clearResetResult() {
        _reset.value = _reset.value.copy(success = false, errorMsg = null)
    }

    fun onResetCodeChange(v: String) {
        val err = if (v.length < 4) "Código demasiado corto" else null
        _reset.value = _reset.value.copy(code = v, codeError = err)
    }

    fun onResetPassChange(v: String) {
        val passErr = validateStrongPassword(v)
        val confirmErr = validateConfirm(v, _reset.value.confirm)
        _reset.value = _reset.value.copy(newPass = v, newPassError = passErr, confirmError = confirmErr)
    }

    fun onResetConfirmChange(v: String) {
        val err = validateConfirm(_reset.value.newPass, v)
        _reset.value = _reset.value.copy(confirm = v, confirmError = err)
    }

    fun submitResetPassword() {
        val s = _reset.value
        val codeErr = if (s.code.length < 4) "Código demasiado corto" else null
        val passErr = validateStrongPassword(s.newPass)
        val confirmErr = validateConfirm(s.newPass, s.confirm)

        if (codeErr != null || passErr != null || confirmErr != null) {
            _reset.value = s.copy(codeError = codeErr, newPassError = passErr, confirmError = confirmErr)
            return
        }

        viewModelScope.launch {
            _reset.value = s.copy(isSubmitting = true, errorMsg = null)

            repo.resetPasswordRemote(email = s.email, code = s.code, newPassword = s.newPass)
                .onSuccess {
                    _reset.value = _reset.value.copy(isSubmitting = false, success = true)
                }
                .onFailure { ex ->
                    _reset.value = _reset.value.copy(
                        isSubmitting = false,
                        errorMsg = ex.message ?: "Error al restablecer contraseña"
                    )
                }
        }
    }
}
