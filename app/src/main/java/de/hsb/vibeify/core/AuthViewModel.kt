package de.hsb.vibeify.core

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.hsb.vibeify.data.repository.AuthRepository
import de.hsb.vibeify.data.repository.AuthRepositoryState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {
    val authState: StateFlow<AuthRepositoryState> = authRepository.state

    fun signOut() {
        viewModelScope.launch(Dispatchers.IO) {
            authRepository.signOut()
        }
    }
}
