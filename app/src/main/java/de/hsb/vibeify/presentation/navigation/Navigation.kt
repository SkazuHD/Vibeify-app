package de.hsb.vibeify.core

import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import de.hsb.vibeify.ui.components.AppHeader.AppHeader
import de.hsb.vibeify.ui.components.StickyBar.StickyBar
import de.hsb.vibeify.ui.home.MainView
import de.hsb.vibeify.ui.login.LoginView
import de.hsb.vibeify.ui.playlist.PlaylistView
import de.hsb.vibeify.ui.playlist.detail.PlaylistDetailView
import de.hsb.vibeify.ui.profile.ProfileView
import de.hsb.vibeify.ui.register.RegisterView
import de.hsb.vibeify.ui.search.SearchView


@Composable
fun AppRouter(authViewModel: AuthViewModel = hiltViewModel()) {
    val authState by authViewModel.authState.collectAsState()

    val destination = when {
        !authState.isAuthResolved -> "loading"
        authState.currentUser != null -> "root"
        else -> "auth"
    }

    Log.d("AppRouter", "Navigating to destination: $destination")

    when (destination) {
        "loading" -> {
            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .systemBarsPadding(),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 1.dp
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.onSurface)
                }
            }
        }

        "auth" -> {
            AuthNavHost()
        }

        "root" -> {
            RootNavHost()
        }
    }
}

@Composable
fun AuthNavHost() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = Destinations.LoginView.route) {
        composable(Destinations.LoginView.route) { LoginView(navController = navController) }
        composable(Destinations.RegisterView.route) { RegisterView() }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RootNavHost() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets.systemBars,
        topBar = {
            val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())
            AppHeader(scrollBehavior, modifier = Modifier)
        },
        bottomBar = {
            var selectedDestination by rememberSaveable { mutableIntStateOf(NavbarDestinations.SONGS.ordinal) }
            Column {
                if (currentRoute != "playback_view") {
                    StickyBar(navController = navController)
                }
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
        }
    ) { contentPadding ->
        NavHost(
            navController = navController,
            startDestination = NavbarDestinations.SONGS.route,
            modifier = Modifier.padding(contentPadding)
        ) {
            NavbarDestinations.entries.forEach { destination ->
                composable(destination.route) {
                    when (destination) {
                        NavbarDestinations.SONGS -> MainView(
                            modifier = Modifier,
                            navController = navController
                        )

                        NavbarDestinations.PLAYLISTS -> PlaylistView(
                            modifier = Modifier,
                            navController = navController
                        )

                        NavbarDestinations.SEARCH -> SearchView(
                            modifier = Modifier,
                            navController = navController
                        )

                        NavbarDestinations.PROFILE -> ProfileView(navController = navController)
                    }
                }
            }
            composable(
                route = Destinations.PlaylistDetailView.route,
                arguments = listOf(navArgument("playlistId") { type = NavType.StringType })
            ) { backStackEntry ->
                val playlistId = backStackEntry.arguments?.getString("playlistId") ?: ""
                PlaylistDetailView(
                    modifier = Modifier,
                    playlistId = playlistId,
                    navController = navController
                )
            }
            composable(Destinations.PlaybackView.route) {
                de.hsb.vibeify.ui.player.MinimalMusicPlayer(
                    nextSong = "Current Queue",
                )
            }
        }
    }
}