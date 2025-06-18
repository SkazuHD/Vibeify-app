package de.hsb.vibeify.ui.register

import androidx.compose.foundation.text.input.TextFieldState
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.hsb.vibeify.data.repository.FirebaseAuthRepo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class RegisterUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val registrationSuccess: Boolean = false
)

@HiltViewModel
class RegisterViewModel @Inject constructor(private val authRepository: FirebaseAuthRepo) :

    ViewModel() {

        private val _uiState = MutableStateFlow(RegisterUiState())
        val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()
        val usernameState = TextFieldState()
        val emailState = TextFieldState()
        val confirmEmailState = TextFieldState()
        val passwordState = TextFieldState()
        val confirmPasswordState = TextFieldState()


    fun register() {
        viewModelScope.launch {
            _uiState.value = RegisterUiState(isLoading = true)
            try {
                authRepository.signUp(emailState.text.toString().trim(), passwordState.text.toString().trim())

            }catch (e: Exception) {
                _uiState.value = RegisterUiState(
                    isLoading = false,
                    error = e.message ?: "Registration failed"
                )
            }

        }
    }
}
