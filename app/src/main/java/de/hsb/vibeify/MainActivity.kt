package de.hsb.vibeify


import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import dagger.hilt.android.AndroidEntryPoint
import de.hsb.vibeify.core.AppRouter
import de.hsb.vibeify.core.ui.theme.VibeifyTheme
import de.hsb.vibeify.ui.login.LoginViewModel


@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val loginViewModel: LoginViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        splashScreen.setKeepOnScreenCondition {
            !loginViewModel.uiState.value.isAuthResolved
        }
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            VibeifyTheme {
                AppRouter()
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    VibeifyTheme {
    }
}