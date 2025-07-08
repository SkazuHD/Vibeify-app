package de.hsb.vibeify.widget.myActivities

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent

import de.hsb.vibeify.core.AppRouter


    class NavigationActivity : ComponentActivity() {
        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)

            val startDestination = intent?.getStringExtra("destination")

            setContent {
                AppRouter(startDestination)
            }
        }
    }