package de.hsb.vibeify.core

import android.util.Log
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navigation
import de.hsb.vibeify.ui.home.MainView
import de.hsb.vibeify.ui.login.LoginView
import de.hsb.vibeify.ui.login.LoginViewModel
import de.hsb.vibeify.ui.playlist.PlaylistView
import de.hsb.vibeify.ui.register.RegisterView
import de.hsb.vibeify.ui.search.SearchView


@Composable
fun AppRouter(authViewModel: LoginViewModel = hiltViewModel()) {
    val rootNavController = rememberNavController()
    val isAuthenticated by authViewModel.uiState.collectAsState()


    LaunchedEffect(isAuthenticated) {
        Log.d("AppRouter", "LaunchedEffect triggered. loginSuccess: ${isAuthenticated.loginSuccess}")
        if (isAuthenticated.loginSuccess) {
            rootNavController.navigate("root") {
                popUpTo("auth") { inclusive = true }
            }
        } else {
            rootNavController.navigate("auth") {
                popUpTo("root") { inclusive = true }
            }
        }
    }

    NavHost(
        navController = rootNavController,
        startDestination = if (isAuthenticated.loginSuccess) "root" else "auth"
    )

    {
        navigation(startDestination = Destinations.LoginView.route, route = "auth") {
            composable(Destinations.LoginView.route) { LoginView(rootNavController, authViewModel) }
            composable(Destinations.RegisterView.route) { RegisterView(rootNavController) }
        }

        navigation(
            startDestination = NavbarDestinations.SONGS.route,
            route = "root"
        ) {
            composable(NavbarDestinations.SONGS.route) { RootNavigationBar() }
        }

    }
}

@Composable
fun AppNavHost(navController: NavHostController,
               startDestination: NavbarDestinations,
               modifier: Modifier = Modifier) {

    NavHost(navController = navController, startDestination = startDestination.route) {
        NavbarDestinations.entries.forEach { destination ->
            composable(destination.route) {
                when (destination) {
                    NavbarDestinations.SONGS -> MainView(navController)
                    NavbarDestinations.PLAYLISTS -> PlaylistView()
                    NavbarDestinations.SEARCH -> SearchView()
                }
            }
        }
    }
}

@Composable
fun RootNavigationBar( modifier: Modifier = Modifier) {
    val navController = rememberNavController()
    val startDestination = NavbarDestinations.SONGS
    var selectedDestination by rememberSaveable { mutableIntStateOf(startDestination.ordinal) }

    Scaffold(
        modifier = modifier,
        bottomBar = {
            NavigationBar(windowInsets = NavigationBarDefaults.windowInsets) {
                NavbarDestinations.entries.forEachIndexed { index, destination ->
                    NavigationBarItem(
                        selected = selectedDestination == index,
                        onClick = {
                            navController.navigate(route = destination.route)
                            selectedDestination = index
                        },
                        icon = {
                            Icon(
                                destination.icon,
                                contentDescription = destination.contentDescription
                            )
                        },
                        label = { Text(destination.label) }
                    )
                }
            }
        }
    ) { contentPadding ->
        AppNavHost(navController, startDestination, modifier = Modifier.padding(contentPadding))
    }
}