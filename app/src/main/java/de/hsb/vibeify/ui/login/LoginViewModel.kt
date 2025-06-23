package de.hsb.vibeify.ui.login

import androidx.compose.foundation.text.input.TextFieldState
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.hsb.vibeify.data.repository.AuthRepository
import de.hsb.vibeify.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject


data class LoginUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val loginSuccess: Boolean = false,
    val isAuthResolved: Boolean = false
)

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()
    val usernameState = TextFieldState()
    val passwordState = TextFieldState()


    init {
        viewModelScope.launch {
            authRepository.state.collect {
                _uiState.value = LoginUiState(
                    isLoading = false,
                    error = null,
                    loginSuccess = it.currentUser != null,
                    isAuthResolved = it.isAuthResolved
                )
            }
        }
    }

    fun signIn() {
        viewModelScope.launch {
            _uiState.value = LoginUiState(isLoading = true)
            try {
                authRepository.signIn(
                    usernameState.text.toString().trim(),
                    passwordState.text.toString().trim()
                )
            } catch (e: Exception) {
                _uiState.value = LoginUiState(
                    isLoading = false,
                    error = e.message ?: "Unbekannter Fehler beim Login"
                )
                return@launch
            }
        }
    }


    fun resetError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}