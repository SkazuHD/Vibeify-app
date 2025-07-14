package de.hsb.vibeify

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import dagger.hilt.android.AndroidEntryPoint
import de.hsb.vibeify.core.AppRouter
import de.hsb.vibeify.core.navigation.NavigationDestination
import de.hsb.vibeify.core.ui.theme.VibeifyTheme
import de.hsb.vibeify.services.AnalyticsService
import de.hsb.vibeify.ui.login.LoginViewModel
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var analyticsService: AnalyticsService
    private val loginViewModel: LoginViewModel by viewModels()
    private var pendingDeepLinkDestination by mutableStateOf<String?>(null)

    // Called when the activity is created
    override fun onCreate(savedInstanceState: Bundle?) {
        // Show splash screen until authentication state is resolved
        val splashScreen = installSplashScreen()
        splashScreen.setKeepOnScreenCondition {
            !loginViewModel.uiState.value.isAuthResolved
        }
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Handle any deep link that launched the app
        val deepLinkDestination = handleDeepLink(intent)
        Log.d("MainActivity", "Deep link destination: $deepLinkDestination")

        // Set up the app's UI with theme and navigation
        setContent {
            VibeifyTheme {
                AppRouter(
                    initialDestination = deepLinkDestination,
                    pendingDestination = pendingDeepLinkDestination,
                    onNavigationHandled = { pendingDeepLinkDestination = null }
                )
            }
        }
    }

    // Handle new intents while app is running
    override fun onNewIntent(intent: Intent) {

        super.onNewIntent(intent)
        setIntent(intent)

        val deepLinkDestination = handleDeepLink(intent)
        Log.d("MainActivity", "New deep link destination: $deepLinkDestination")

        if (deepLinkDestination != null) {
            pendingDeepLinkDestination = deepLinkDestination
            Log.d("MainActivity", "Set pending destination: $deepLinkDestination")
        }
    }

    // Extracts navigation destination from an Intent, either via URI or extras
    private fun handleDeepLink(intent: Intent): String? {
        return when {
            intent.action == Intent.ACTION_VIEW && intent.data != null -> {
                val uri = intent.data
                Log.d("MainActivity", "Handling deep link: ${uri}")
                Log.d("MainActivity", "Host: ${uri?.host}")
                Log.d("MainActivity", "Path: ${uri?.path}")

                // Parse destination from host or path and map to navigation route
                val destination = uri?.host ?: uri?.path?.removePrefix("/")
                Log.d("MainActivity", "Parsed destination: $destination")
                when (destination) {
                    "playback" -> NavigationDestination.Detail.Playback.route
                    "home" -> NavigationDestination.Main.Home.route
                    "search" -> NavigationDestination.Main.Search.route
                    "playlists" -> NavigationDestination.Main.Playlists.route
                    "profile" -> NavigationDestination.Main.Profile.route
                    else -> null
                }
            }

            // Handle intent with an explicit "destination" extra
            intent.hasExtra("destination") -> {
                intent.getStringExtra("destination")
            }

            else -> null
        }
    }
}

// Basic Composable preview used for UI inspection in Android Studio
@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    VibeifyTheme {
    }
}