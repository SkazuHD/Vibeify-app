package de.hsb.vibeify.ui.Views

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicSecureTextField
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.TextObfuscationMode
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff

import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedSecureTextField
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SecureTextField
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import de.hsb.vibeify.viewmodel.UserState
import kotlinx.coroutines.launch

@Composable
fun LoginView(navController: NavController, modifier: Modifier = Modifier) {
    val coroutineScope = rememberCoroutineScope()
    val vm = UserState.current
    var username by remember { mutableStateOf("") }
    val usernameState = remember { TextFieldState() }
    val passwordState = remember { TextFieldState() }

    Box(
        modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center
    ){
        Column(

        ) {
            Text(
                text = "Login",
                textAlign = TextAlign.Center,
                fontSize = 30.sp,
                modifier = Modifier.padding(0.dp, 0.dp, 0.dp, 20.dp)
            )

            OutlinedTextField(
                state = usernameState,
                label = { Text("Username") },
            )

            OutlinedSecureTextField(
                state = passwordState,
                label = { Text("Password") },

                )


            Button(
                onClick = {
                    coroutineScope.launch {
                        vm.signIn("email", "password")
                    }
                }
            ) {
                Text("Login")
            }
            Button(
                onClick = {
                    navController.navigate(Destinations.RegisterView.route)
                }
            ) {
                Text("Register NOW")
            }
        }

    }

}