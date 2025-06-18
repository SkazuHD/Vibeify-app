package de.hsb.vibeify.ui.register

import androidx.compose.foundation.text.input.TextFieldState
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

data class RegisterUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val registrationSuccess: Boolean = false
)

@HiltViewModel
class RegisterViewModel @Inject constructor() :

    ViewModel() {

        private val _uiState = MutableStateFlow(RegisterUiState())
        val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()
        val usernameState = TextFieldState()
        val emailState = TextFieldState()
        val confirmEmailState = TextFieldState()
        val passwordState = TextFieldState()
        val confirmPasswordState = TextFieldState()


    fun register() {
        // Registration logic will be implemented here
        // This is a placeholder for the actual registration logic
        _uiState.value = RegisterUiState(isLoading = true)

        // Simulate a successful registration
        _uiState.value = RegisterUiState(registrationSuccess = true)
    }
}