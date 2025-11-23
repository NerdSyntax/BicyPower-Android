package com.example.bicypower.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.bicypower.data.local.database.BicyPowerDatabase
import com.example.bicypower.data.repository.UserRepository
import com.example.bicypower.data.remote.dto.UsuarioDtoRemote
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// Modelo que usamos en la UI del admin
data class AdminUserUi(
    val id: Long,
    val name: String,
    val email: String,
    val role: String,
    val phone: String
)

data class AdminUsersState(
    val users: List<AdminUserUi> = emptyList(),
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

    // En realidad no usamos el dao aquí, pero lo dejamos para no romper otras cosas
    private val userDao = BicyPowerDatabase.getInstance(app).userDao()
    private val repo = UserRepository(userDao)

    private val _state = MutableStateFlow(AdminUsersState())
    val state = _state.asStateFlow()

    init {
        loadUsers()
    }

    // -------- Cargar usuarios desde el microservicio --------
    fun loadUsers() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, errorMsg = null)
            repo.getAllUsersRemote()
                .onSuccess { list ->
                    _state.value = _state.value.copy(
                        isLoading = false,
                        users = list.map { it.toAdminUi() },
                        errorMsg = null
                    )
                }
                .onFailure { e ->
                    _state.value = _state.value.copy(
                        isLoading = false,
                        errorMsg = e.message ?: "Error al cargar usuarios"
                    )
                }
        }
    }

    // -------- Crear Staff --------
    fun openCreate() {
        _state.value = _state.value.copy(showCreate = true, errorMsg = null)
    }

    fun closeCreate() {
        _state.value = _state.value.copy(
            showCreate = false,
            formName = "",
            formEmail = "",
            formPhone = "",
            formPass = "",
            errorMsg = null
        )
    }

    fun onFormName(v: String) {
        _state.value = _state.value.copy(formName = v)
    }

    fun onFormEmail(v: String) {
        _state.value = _state.value.copy(formEmail = v)
    }

    fun onFormPhone(v: String) {
        _state.value = _state.value.copy(formPhone = v)
    }

    fun onFormPass(v: String) {
        _state.value = _state.value.copy(formPass = v)
    }

    /** crea un nuevo STAFF en el microservicio usando /api/usuarios */
    // -------- Crear Staff --------
    fun createStaff() {
        val s = _state.value
        if (s.isSubmitting) return

        viewModelScope.launch {
            _state.value = s.copy(isSubmitting = true, errorMsg = null)

            repo.registerStaffRemote(
                name = s.formName,
                email = s.formEmail,
                phone = s.formPhone,
                pass = s.formPass
            ).onSuccess {
                _state.value = _state.value.copy(
                    isSubmitting = false,
                    showCreate = false,
                    formName = "",
                    formEmail = "",
                    formPhone = "",
                    formPass = ""
                )
                loadUsers() // refresca la lista
            }.onFailure { e ->
                _state.value = _state.value.copy(
                    isSubmitting = false,
                    errorMsg = e.message ?: "Error al crear staff"
                )
            }
        }
    }

    // -------- Eliminar usuario --------
    fun askDelete(id: Long) {
        _state.value = _state.value.copy(confirmDeleteId = id)
    }

    fun cancelDelete() {
        _state.value = _state.value.copy(confirmDeleteId = null)
    }

    fun confirmDelete() {
        val id = _state.value.confirmDeleteId ?: return
        viewModelScope.launch {
            _state.value = _state.value.copy(isSubmitting = true)
            repo.deleteUserRemote(id)
                .onSuccess {
                    _state.value = _state.value.copy(
                        isSubmitting = false,
                        confirmDeleteId = null
                    )
                    loadUsers()
                }
                .onFailure { e ->
                    _state.value = _state.value.copy(
                        isSubmitting = false,
                        errorMsg = e.message ?: "No se pudo eliminar el usuario",
                        confirmDeleteId = null
                    )
                }
        }
    }
}

// ---------- Mapper de DTO remoto a modelo de UI ----------
private fun UsuarioDtoRemote.toAdminUi() = AdminUserUi(
    id = this.id ?: 0L,
    name = this.nombre ?: "",
    email = this.email ?: "",
    role = this.rol ?: "",
    phone = this.telefono ?: ""
)
