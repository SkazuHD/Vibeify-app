package de.hsb.vibeify.ui.register

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
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
    val registrationSuccess: Boolean = false,
    val emailError: String = "",
    val confirmEmailError: String = "",
    val passwordError: String = "",
    val confirmPasswordError: String = "",
    val generalError: String? = null
)

@HiltViewModel
class RegisterViewModel @Inject constructor(private val authRepository: FirebaseAuthRepo) :

    ViewModel() {

        private val _uiState = MutableStateFlow(RegisterUiState())
        val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()
        val emailState = TextFieldState()
        val confirmEmailState = TextFieldState()
        val passwordState = TextFieldState()
        val confirmPasswordState = TextFieldState()


    fun register() {

        if (validate()){
            viewModelScope.launch {
                _uiState.value = RegisterUiState(isLoading = true)
                try {
                    authRepository.signUp(emailState.text.toString().trim(), passwordState.text.toString().trim())

                }catch (e: Exception) {
                    _uiState.value = RegisterUiState(
                        isLoading = false,
                        generalError = e.message ?: "Registration failed"
                    )
                }

            }
        }
    }

    fun validate() : Boolean {
        var isValid = true
        _uiState.value = RegisterUiState(
            emailError = "",
            confirmEmailError = "",
            passwordError = "",
            confirmPasswordError = ""
        )
        //validate email
        if (emailState.text.isBlank()) {
            _uiState.value = _uiState.value.copy(emailError = "Email cannot be empty")
            isValid = false
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(emailState.text).matches()) {
            _uiState.value = _uiState.value.copy(emailError = "Invalid email format")
            isValid = false
        }

        //validate password
        if(passwordState.text.isBlank()) {
            _uiState.value = _uiState.value.copy(passwordError = "Password cannot be empty")
            isValid = false
        } else if (!passwordState.text.matches(Regex("^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{6,}$"))) {
            _uiState.value = _uiState.value.copy(passwordError = "Password must be at least 6 characters long and contain letters and numbers")
            isValid = false
        }

        //match emails
        if(emailState.text.isNotBlank() && confirmEmailState.text.isNotBlank() &&
            emailState.text != confirmEmailState.text) {
            _uiState.value = _uiState.value.copy(
                confirmEmailError = "email does not match"
            )
            isValid = false
        }
        //match passwords
        if(passwordState.text.isNotBlank() && confirmPasswordState.text.isNotBlank() &&
            passwordState.text != confirmPasswordState.text) {
            _uiState.value = _uiState.value.copy(
                confirmPasswordError = "password does not match"
            )
            isValid = false
        }
    return isValid
    }

}
