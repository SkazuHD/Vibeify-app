package de.hsb.vibeify


import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import dagger.hilt.android.AndroidEntryPoint
import de.hsb.vibeify.core.AppRouter
import de.hsb.vibeify.core.ui.theme.VibeifyTheme
import de.hsb.vibeify.data.repository.FirestoreRepo


@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()

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