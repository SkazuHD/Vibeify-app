package de.hsb.vibeify

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import de.hsb.vibeify.ui.Views.Destinations
import de.hsb.vibeify.ui.Views.LoginView
import de.hsb.vibeify.ui.Views.MainView
import de.hsb.vibeify.ui.Views.NavbarDestinations
import de.hsb.vibeify.ui.Views.PlaylistDetailView
import de.hsb.vibeify.ui.Views.PlaylistView
import de.hsb.vibeify.ui.Views.ProfileView
import de.hsb.vibeify.ui.Views.RegisterView
import de.hsb.vibeify.ui.Views.SearchView


@Composable
fun GuardRouter(modifier: Modifier = Modifier) {
    val navController = rememberNavController()
    val startDestination = Destinations.Guard

    NavHost(navController = navController, startDestination = startDestination.route) {
        composable(Destinations.Guard.route) {
            Guard(
                navController = navController,
                modifier = Modifier.padding(16.dp),
            )
        }
        composable(Destinations.LoginView.route) {
            LoginView(
                navController = navController,
            )

        }
        composable(Destinations.RegisterView.route) {
            RegisterView(
                navController = navController,
            )

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

        Destinations.entries.forEach { destination ->
            composable(destination.route) {
                when (destination) {
                    Destinations.MainView -> MainView(navController)
                    Destinations.LoginView -> LoginView(navController)
                    Destinations.RegisterView -> RegisterView(navController)
                    Destinations.ProfileView -> ProfileView()
                    Destinations.PlaylistView -> PlaylistView()
                    Destinations.PlaylistDetailView -> PlaylistDetailView()
                    Destinations.SearchView -> SearchView()
                    Destinations.Guard -> Guard(navController)
                }
            }
        }

    }
}

@Composable
fun RootNavigationBar(modifier: Modifier = Modifier) {
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