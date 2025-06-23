package de.hsb.vibeify.services

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class PlayerViewModel @Inject constructor(
    val playerServiceV2: PlayerServiceV2
) : ViewModel() {
    val player = playerServiceV2.getController()
}