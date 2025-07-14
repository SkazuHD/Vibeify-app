package de.hsb.vibeify.ui.register

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.hsb.vibeify.data.repository.FirebaseAuthRepo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

data class RegisterUiState(
    val isLoading: Boolean = false,
    val registrationSuccess: Boolean = false,
    val emailError: String = "",
    val confirmEmailError: String = "",
    val passwordError: String = "",
    val confirmPasswordError: String = "",
    val generalError: String? = null,
    val hasErrors: Boolean = false
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
        val emailTouched = mutableStateOf(false)
        val confirmEmailTouched = mutableStateOf(false)
        val passwordTouched = mutableStateOf(false)
        val confirmPasswordTouched = mutableStateOf(false)
        val emailHadFocus = mutableStateOf(false)
        val confirmEmailHadFocus = mutableStateOf(false)
        val passwordHadFocus = mutableStateOf(false)
        val confirmPasswordHadFocus = mutableStateOf(false)

    // Observes changes in each input field's text and triggers validation & error state updates
    init {
        viewModelScope.launch {
            launch {
                snapshotFlow { emailState.text }.collectLatest {
                    validateEmail()
                    hasErrors()
                }
            }
            launch {
                snapshotFlow { confirmEmailState.text }.collectLatest {
                    validateConfirmEmail()
                    hasErrors()
                }
            }
            launch {
                snapshotFlow { passwordState.text }.collectLatest {
                    validatePassword()
                    hasErrors()
                }
            }
            launch {
                snapshotFlow { confirmPasswordState.text }.collectLatest {
                    validateConfirmPassword()
                    hasErrors()
                }
            }
        }
    }


    // Attempts to register user with email and password, updates loading and error states accordingly
    fun register() {
            viewModelScope.launch {
                _uiState.value = _uiState.value.copy(isLoading = true)
                try {
                    authRepository.signUp(emailState.text.toString().trim(), passwordState.text.toString().trim())

                }catch (e: Exception) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        generalError = e.message ?: "Unknown error during registration"
                    )
                }
            }

    }

    // Validates email format and emptiness; sets emailError accordingly
    fun validateEmail(){
        _uiState.value = _uiState.value.copy(emailError = "")
        if (emailState.text.isBlank()) {
            _uiState.value = _uiState.value.copy(emailError = "Email cannot be empty")
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(emailState.text).matches()) {
            _uiState.value = _uiState.value.copy(emailError = "Invalid email format")
        }
    }

    // Validates that confirmEmail matches email; sets confirmEmailError if not
    fun validateConfirmEmail(){
        _uiState.value = _uiState.value.copy(confirmEmailError = "")
        if(emailState.text.isNotBlank() && confirmEmailState.text.isNotBlank() &&
            emailState.text != confirmEmailState.text) {
            _uiState.value = _uiState.value.copy(
                confirmEmailError = "email does not match"
            )
        }

    }

    // Validates password complexity and emptiness
    fun validatePassword(){
        _uiState.value = _uiState.value.copy(passwordError = "")
        if(passwordState.text.isBlank()) {
            _uiState.value = _uiState.value.copy(passwordError = "Password cannot be empty")
        } else if (!passwordState.text.matches(Regex("^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{6,}$"))) {
            _uiState.value = _uiState.value.copy(passwordError = "Password must be at least 6 characters long and contain letters and numbers")
        }

    }
    // Validates that confirmPassword matches password
    fun validateConfirmPassword(){
        _uiState.value = _uiState.value.copy(confirmPasswordError = "")
        if(passwordState.text.isNotBlank() && confirmPasswordState.text.isNotBlank() &&
            passwordState.text != confirmPasswordState.text) {
            _uiState.value = _uiState.value.copy(
                confirmPasswordError = "password does not match"
            )
        }

    }

    // Checks if any validation error exists and updates the hasErrors flag accordingly
    fun hasErrors() {
        if(uiState.value.emailError.isNotEmpty() ||
            uiState.value.confirmEmailError.isNotEmpty() ||
            uiState.value.passwordError.isNotEmpty() ||
            uiState.value.confirmPasswordError.isNotEmpty()) {
            _uiState.value = _uiState.value.copy(hasErrors = true)
        }
        else {
            _uiState.value = _uiState.value.copy(hasErrors = false)
        }
    }
}
