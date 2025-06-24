package de.hsb.vibeify.services

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.session.MediaController
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PlayerViewModel @Inject constructor(
    val playerServiceV2: PlayerServiceV2
) : ViewModel() {
    val service = playerServiceV2
    private val _controller = MutableStateFlow<MediaController?>(null)
    val controller: StateFlow<MediaController?> = _controller

    init {
        viewModelScope.launch {
            _controller.value = service.awaitController()
           // service.demoPlayBack()
        }
    }
}