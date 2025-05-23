package de.hsb.vibeify

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import de.hsb.vibeify.ui.Views.LoginView
import de.hsb.vibeify.viewmodel.LoginViewModel

@Composable
fun Guard(
    navController: NavController, modifier: Modifier = Modifier,
    vm: LoginViewModel = hiltViewModel()
) {

    val userState = vm.uiState.collectAsState().value


    if (!userState.loginSuccess) {
        LoginView(navController)
    } else {
        RootNavigationBar()
    }
}