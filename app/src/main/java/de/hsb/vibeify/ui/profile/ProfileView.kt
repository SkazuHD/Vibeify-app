package de.hsb.vibeify.ui.profile

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import de.hsb.vibeify.core.AuthViewModel
import de.hsb.vibeify.ui.components.Avatar

@Composable
fun ProfileView(modifier: Modifier = Modifier, viewModel: ProfileViewModel = hiltViewModel(), authViewModel: AuthViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        when {
            uiState.isLoading -> {
                CircularProgressIndicator()
            }
            uiState.error != null -> {
                Text(text = "Error: ${uiState.error}")
            }
            else -> {
                Avatar(
                    size = 150.dp,
                    initials = "JL"
                )

                uiState.user?.let { user ->
                    Text(text = "Username: ${user.name}")
                    Text(text = "Email: ${user.email}")
                } ?: Text(text = "No user information available")

                Button(
                    onClick = {
                        authViewModel.signOut()
                    },
                    modifier = Modifier.padding(top = 16.dp)
                ) {
                    Text(text = "Sign Out")
                }

            }
        }
    }

}