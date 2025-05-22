package de.hsb.vibeify

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import de.hsb.vibeify.ui.Views.LoginView
import de.hsb.vibeify.ui.Views.MainView
import de.hsb.vibeify.ui.Views.Views

@Composable
fun MainNavigation() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Views.MainView.route) {
        composable(Views.MainView.route) {
           MainView(
                navController = navController,
                modifier = Modifier.padding(16.dp),
            )
            }
        composable(Views.LoginView.route) {
            LoginView(
                modifier = Modifier.padding(16.dp),
            )
        }
    }
}