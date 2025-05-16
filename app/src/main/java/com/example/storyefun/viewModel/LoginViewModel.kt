package com.example.storyefun.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.storyefun.data.repository.AuthRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val errorMessage: String? = null,
    val isLoading: Boolean = false,
    val navigateToHome: Boolean = false,
    val navigateToRegister: Boolean = false,
    val navigateToAdmin: Boolean = false
)

class LoginViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun updateEmail(email: String) {
        _uiState.value = _uiState.value.copy(email = email, errorMessage = null)
    }

    fun updatePassword(password: String) {
        _uiState.value = _uiState.value.copy(password = password, errorMessage = null)
    }

    fun login() {
        val state = _uiState.value
        when {
            state.email.isBlank() || state.password.isBlank() -> {
                _uiState.value = state.copy(errorMessage = "All fields are required")
            }
            else -> {
                _uiState.value = state.copy(isLoading = true, errorMessage = null)
                viewModelScope.launch {
                    val result = authRepository.loginUser(state.email, state.password)
                    _uiState.value = _uiState.value.copy(isLoading = false)
                    if (result.isSuccess) {
                        // Check user role after successful login
                        val user = FirebaseAuth.getInstance().currentUser
                        if (user != null) {
                            try {
                                val db = FirebaseFirestore.getInstance()
                                val document = db.collection("users")
                                    .document(user.uid)
                                    .get()
                                    .await()
                                val role = document.getString("role")
                                if (role == "admin") {
                                    _uiState.value = _uiState.value.copy(navigateToAdmin = true)
                                } else {
                                    _uiState.value = _uiState.value.copy(navigateToHome = true)
                                }
                            } catch (e: Exception) {
                                // Handle Firestore errors, default to home
                                _uiState.value = _uiState.value.copy(
                                    navigateToHome = true,
                                    errorMessage = "Unable to verify user role: ${e.message}"
                                )
                            }
                        } else {
                            _uiState.value = _uiState.value.copy(
                                errorMessage = "User not found after login"
                            )
                        }
                    } else {
                        _uiState.value = _uiState.value.copy(
                            errorMessage = result.exceptionOrNull()?.message ?: "Login failed"
                        )
                    }
                }
            }
        }
    }

    fun navigateToRegister() {
        _uiState.value = _uiState.value.copy(navigateToRegister = true)
    }

    fun resetNavigation() {
        _uiState.value = _uiState.value.copy(
            navigateToHome = false,
            navigateToRegister = false,
            navigateToAdmin = false
        )
    }
}