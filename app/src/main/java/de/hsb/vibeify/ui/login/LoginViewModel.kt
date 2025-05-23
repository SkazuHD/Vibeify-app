package de.hsb.vibeify.ui.login

import android.util.Log
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
    val usernameState = TextFieldState()
    val passwordState = TextFieldState()

    fun signIn() {
        viewModelScope.launch {
            _uiState.value = LoginUiState(isLoading = true)
            val result = userRepository.signIn(
                usernameState.text.toString(),
                passwordState.text.toString()
            )
            result.fold(
                onSuccess = { user ->
                    Log.d("LoginViewModel", "signIn successful, updating UI state.")
                    _uiState.value = LoginUiState(loginSuccess = true)
                },
                onFailure = { exception ->
                    Log.e("LoginViewModel", "signIn failed: ${exception.message}")
                    _uiState.value = LoginUiState(error = exception.message ?: "Login failed")
                }
            )
        }
    }


    fun resetError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}