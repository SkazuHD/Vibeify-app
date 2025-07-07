package de.hsb.vibeify.core

import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowDown
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import de.hsb.vibeify.core.navigation.NavigationDestination
import de.hsb.vibeify.core.navigation.bottomNavDestinations
import de.hsb.vibeify.core.navigation.rememberVibeifyNavigationController
import de.hsb.vibeify.core.navigation.urlDecode
import de.hsb.vibeify.ui.components.AppHeader.AppHeader
import de.hsb.vibeify.ui.components.StickyBar.StickyBar
import de.hsb.vibeify.ui.home.MainView
import de.hsb.vibeify.ui.login.LoginView
import de.hsb.vibeify.ui.player.MinimalMusicPlayer
import de.hsb.vibeify.ui.playlist.PlaylistView
import de.hsb.vibeify.ui.playlist.detail.PlaylistDetailView
import de.hsb.vibeify.ui.profile.ProfileView
import de.hsb.vibeify.ui.publicProfile.PublicProfileView
import de.hsb.vibeify.ui.register.RegisterView
import de.hsb.vibeify.ui.search.SearchView

@Composable
fun AppRouter(authViewModel: AuthViewModel = hiltViewModel()) {
    val authState by authViewModel.authState.collectAsState()

    when {
        !authState.isAuthResolved -> LoadingScreen()
        authState.currentUser != null -> AuthenticatedNavigation()
        else -> UnauthenticatedNavigation()
    }
}

@Composable
private fun LoadingScreen() {
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

@Composable
private fun UnauthenticatedNavigation() {
    val navController = rememberNavController()
    val navigationController = rememberVibeifyNavigationController(navController)

    NavHost(
        navController = navController,
        startDestination = NavigationDestination.Login.route
    ) {
        composable(NavigationDestination.Login.route) {
            LoginView(navController = navController)
        }
        composable(NavigationDestination.Register.route) {
            RegisterView()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AuthenticatedNavigation() {
    val navController = rememberNavController()
    val navigationController = rememberVibeifyNavigationController(navController)
    val currentDestination = navigationController.getCurrentDestination()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets.systemBars,
        topBar = {
            if (navigationController.shouldShowAppBar()) {
                val scrollBehavior =
                    TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())
                val arrowDirection =
                    if (currentDestination is NavigationDestination.Detail.Playback)
                        Icons.Default.KeyboardArrowDown
                    else
                        Icons.AutoMirrored.Filled.KeyboardArrowLeft
                AppHeader(
                    scrollBehavior = scrollBehavior,
                    icon = arrowDirection,
                    onBackClick = { navigationController.navigateBack() }
                )
            }
        },
        bottomBar = {
            Column {
                if (navigationController.shouldShowStickyBar()) {
                    StickyBar(navController = navController)
                }

                BottomNavigationBar(
                    currentDestination = currentDestination,
                    onNavigate = { destination ->
                        navigationController.navigateToMainAndClearStack(destination)
                    }
                )
            }
        }
    ) { contentPadding ->
        NavHost(
            navController = navController,
            startDestination = NavigationDestination.Main.Home.route,
            modifier = Modifier.padding(contentPadding)
        ) {
            composable(
                NavigationDestination.Main.Home.route,
                enterTransition = {
                    when (initialState.destination.route) {
                        NavigationDestination.Main.Search.route,
                        NavigationDestination.Main.Playlists.route,
                        NavigationDestination.Main.Profile.route ->
                            slideInHorizontally(
                                initialOffsetX = { -it },
                                animationSpec = tween(300)
                            )

                        else -> null
                    }
                },
                exitTransition = {
                    when (targetState.destination.route) {
                        NavigationDestination.Main.Search.route,
                        NavigationDestination.Main.Playlists.route,
                        NavigationDestination.Main.Profile.route ->
                            slideOutHorizontally(
                                targetOffsetX = { -it },
                                animationSpec = tween(300)
                            )

                        else -> null
                    }
                }
            ) {
                MainView(
                    modifier = Modifier,
                    navController = navController
                )
            }

            composable(
                NavigationDestination.Main.Search.route,
                enterTransition = {
                    when (initialState.destination.route) {
                        NavigationDestination.Main.Home.route ->
                            slideInHorizontally(
                                initialOffsetX = { it },
                                animationSpec = tween(300)
                            )

                        NavigationDestination.Main.Playlists.route,
                        NavigationDestination.Main.Profile.route ->
                            slideInHorizontally(
                                initialOffsetX = { -it },
                                animationSpec = tween(300)
                            )

                        else -> null
                    }
                },
                exitTransition = {
                    when (targetState.destination.route) {
                        NavigationDestination.Main.Home.route ->
                            slideOutHorizontally(
                                targetOffsetX = { it },
                                animationSpec = tween(300)
                            )

                        NavigationDestination.Main.Playlists.route,
                        NavigationDestination.Main.Profile.route ->
                            slideOutHorizontally(
                                targetOffsetX = { -it },
                                animationSpec = tween(300)
                            )

                        else -> null
                    }
                }
            ) {
                SearchView(
                    modifier = Modifier,
                    navController = navController
                )
            }

            composable(
                NavigationDestination.Main.Playlists.route,
                enterTransition = {
                    when (initialState.destination.route) {
                        NavigationDestination.Main.Home.route,
                        NavigationDestination.Main.Search.route ->
                            slideInHorizontally(
                                initialOffsetX = { it },
                                animationSpec = tween(300)
                            )

                        NavigationDestination.Main.Profile.route ->
                            slideInHorizontally(
                                initialOffsetX = { -it },
                                animationSpec = tween(300)
                            )

                        else -> null
                    }
                },
                exitTransition = {
                    when (targetState.destination.route) {
                        NavigationDestination.Main.Home.route,
                        NavigationDestination.Main.Search.route ->
                            slideOutHorizontally(
                                targetOffsetX = { it },
                                animationSpec = tween(300)
                            )

                        NavigationDestination.Main.Profile.route ->
                            slideOutHorizontally(
                                targetOffsetX = { -it },
                                animationSpec = tween(300)
                            )

                        else -> null
                    }
                }
            ) {
                PlaylistView(
                    modifier = Modifier,
                    navController = navController
                )
            }

            composable(
                NavigationDestination.Main.Profile.route,
                enterTransition = {
                    when (initialState.destination.route) {
                        NavigationDestination.Main.Home.route,
                        NavigationDestination.Main.Search.route,
                        NavigationDestination.Main.Playlists.route ->
                            slideInHorizontally(
                                initialOffsetX = { it },
                                animationSpec = tween(300)
                            )

                        else -> null
                    }
                },
                exitTransition = {
                    when (targetState.destination.route) {
                        NavigationDestination.Main.Home.route,
                        NavigationDestination.Main.Search.route,
                        NavigationDestination.Main.Playlists.route ->
                            slideOutHorizontally(
                                targetOffsetX = { it },
                                animationSpec = tween(300)
                            )

                        else -> null
                    }
                }
            ) {
                ProfileView(navController = navController)
            }

            composable(
                route = NavigationDestination.Detail.PublicProfile.ROUTE_TEMPLATE,
                enterTransition = { slideInHorizontally(initialOffsetX = { 300 }) },
                exitTransition = { slideOutHorizontally(targetOffsetX = { 300 }) },
                arguments = listOf(navArgument("userId") { type = NavType.StringType })

            ) { backStackEntry ->
                val userId = backStackEntry.arguments?.getString("userId") ?: ""
                PublicProfileView(
                    modifier = Modifier,
                    navController = navController,
                    userId = userId
                )
            }

            composable(
                route = NavigationDestination.Detail.PlaylistDetail.ROUTE_TEMPLATE,
                enterTransition = { slideInHorizontally(initialOffsetX = { 300 }) },
                exitTransition = { slideOutHorizontally(targetOffsetX = { 300 }) },
                arguments = listOf(navArgument("playlistId") { type = NavType.StringType })
            ) { backStackEntry ->
                val playlistId = backStackEntry.arguments?.getString("playlistId") ?: ""
                val urlDecodedId = playlistId.urlDecode()
                PlaylistDetailView(
                    modifier = Modifier,
                    playlistId = urlDecodedId,
                    navController = navController
                )
            }

            composable(
                NavigationDestination.Detail.Playback.route,
                enterTransition = { slideInVertically(initialOffsetY = { 300 }) },
                exitTransition = { slideOutVertically(targetOffsetY = { 300 }) }) {
                MinimalMusicPlayer(
                    nextSong = "Current Queue"
                )
            }
        }
    }
}

@Composable
private fun BottomNavigationBar(
    currentDestination: NavigationDestination?,
    onNavigate: (NavigationDestination.Main) -> Unit
) {
    NavigationBar(windowInsets = NavigationBarDefaults.windowInsets) {
        bottomNavDestinations.forEach { destination ->
            val isSelected = when (currentDestination) {
                is NavigationDestination.Main -> currentDestination.route == destination.route
                else -> false
            }

            NavigationBarItem(
                selected = isSelected,
                onClick = { onNavigate(destination) },
                icon = {
                    Icon(
                        destination.icon,
                        contentDescription = destination.label
                    )
                },
                label = { Text(destination.label) }
            )
        }
    }
}
