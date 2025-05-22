package de.hsb.vibeify


import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import de.hsb.vibeify.ui.theme.VibeifyTheme
import de.hsb.vibeify.viewmodel.UserState
import de.hsb.vibeify.viewmodel.UserStateViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        val userState by viewModels<UserStateViewModel>()
        val splashScreen = installSplashScreen()

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            VibeifyTheme {
                CompositionLocalProvider(UserState provides userState) {
                    GuardRouter()
                }
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