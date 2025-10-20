package com.example.bicypower.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.bicypower.data.local.database.BicyPowerDatabase
import com.example.bicypower.data.local.user.UserEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class AdminUsersState(
    val users: List<UserEntity> = emptyList(),
    val isLoading: Boolean = true,
    val showCreate: Boolean = false,
    val isSubmitting: Boolean = false,
    val errorMsg: String? = null,
    val formName: String = "",
    val formEmail: String = "",
    val formPhone: String = "",
    val formPass: String = "",
    val confirmDeleteId: Long? = null
)

class AdminUsersViewModel(app: Application) : AndroidViewModel(app) {
    private val dao = BicyPowerDatabase.getInstance(app).userDao()

    private val _state = MutableStateFlow(AdminUsersState())
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            dao.observeAll().collectLatest { list ->
                _state.value = _state.value.copy(users = list, isLoading = false)
            }
        }
    }

    fun openCreate() { _state.value = _state.value.copy(showCreate = true, errorMsg = null) }
    fun closeCreate(){ _state.value = _state.value.copy(showCreate = false, isSubmitting = false, errorMsg = null,
        formName = "", formEmail = "", formPhone = "", formPass = "") }

    fun onFormName(v:String){ _state.value = _state.value.copy(formName = v) }
    fun onFormEmail(v:String){ _state.value = _state.value.copy(formEmail = v) }
    fun onFormPhone(v:String){ _state.value = _state.value.copy(formPhone = v) }
    fun onFormPass(v:String){ _state.value = _state.value.copy(formPass = v) }

    fun createStaff() {
        val s = _state.value
        if (s.formName.isBlank() || s.formEmail.isBlank() || s.formPhone.isBlank() || s.formPass.isBlank()) {
            _state.value = s.copy(errorMsg = "Completa todos los campos"); return
        }
        viewModelScope.launch {
            _state.value = _state.value.copy(isSubmitting = true, errorMsg = null)
            val exists = withContext(Dispatchers.IO) { dao.getByEmail(s.formEmail.trim()) != null }
            if (exists) {
                _state.value = _state.value.copy(isSubmitting = false, errorMsg = "El correo ya existe"); return@launch
            }
            withContext(Dispatchers.IO) {
                dao.insert(
                    UserEntity(
                        name = s.formName.trim(),
                        email = s.formEmail.trim(),
                        phone = s.formPhone.trim(),
                        password = s.formPass,
                        role = "STAFF"
                    )
                )
            }
            closeCreate()
        }
    }

    fun askDelete(id: Long) { _state.value = _state.value.copy(confirmDeleteId = id) }
    fun cancelDelete() { _state.value = _state.value.copy(confirmDeleteId = null) }
    fun confirmDelete() {
        val id = _state.value.confirmDeleteId ?: return
        viewModelScope.launch {
            withContext(Dispatchers.IO) { dao.deleteById(id) }
            cancelDelete()
        }
    }
}
