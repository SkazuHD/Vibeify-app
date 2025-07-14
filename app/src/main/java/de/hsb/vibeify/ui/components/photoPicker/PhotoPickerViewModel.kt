package de.hsb.vibeify.ui.components.photoPicker

import android.net.Uri
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import de.hsb.vibeify.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

// ViewModel for managing the state of the photo picker
@HiltViewModel
class PhotoPickerViewModel @Inject constructor(
    private val userRepository: UserRepository,
    ) : ViewModel() {

        val currentUser = userRepository.state.value.currentUser
        private val _state = MutableStateFlow(currentUser?.imageUrl)
        var state : MutableStateFlow<String?> = _state


    }


