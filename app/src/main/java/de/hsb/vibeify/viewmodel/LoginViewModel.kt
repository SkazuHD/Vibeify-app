package de.hsb.vibeify.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.hsb.vibeify.repo.FirebaseAuthRepo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject


data class LoginUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val loginSuccess: Boolean = false
)

@HiltViewModel
class LoginViewModel @Inject constructor(private val userRepository: FirebaseAuthRepo) :
    ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun signIn(email: String, password: String) {
        viewModelScope.launch {
            _uiState.value = LoginUiState(isLoading = true)
            val result = userRepository.signIn(email, password)
            result.fold(
                onSuccess = { user ->
                    _uiState.value = LoginUiState(loginSuccess = true)
                },
                onFailure = { exception ->
                    _uiState.value = LoginUiState(error = exception.message ?: "Login failed")
                }
            )
        }
    }


    fun resetError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}