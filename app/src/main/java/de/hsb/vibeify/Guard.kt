package de.hsb.vibeify

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import de.hsb.vibeify.ui.Views.LoginView
import de.hsb.vibeify.viewmodel.UserState

@Composable
fun Guard(navController: NavController, modifier: Modifier = Modifier) {
    val vm = UserState.current

    if (!vm.isLoggedIn){
        LoginView(navController)
    }else{
        RootNavigationBar()
    }
}