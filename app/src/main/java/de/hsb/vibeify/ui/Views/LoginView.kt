package de.hsb.vibeify.ui.Views

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedSecureTextField
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
    val usernameState = remember { TextFieldState() }
    val passwordState = remember { TextFieldState() }

    Box(
        modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center
    ) {
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
                        vm.signIn(usernameState.text.toString(), passwordState.text.toString())
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