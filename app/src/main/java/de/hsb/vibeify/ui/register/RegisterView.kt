package de.hsb.vibeify.ui.register

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedSecureTextField
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import de.hsb.vibeify.core.Destinations

@Composable
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
fun RegisterView(navController: NavController, modifier: Modifier = Modifier, vm: RegisterViewModel = hiltViewModel()) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Column(

        ) {
            Text(
                text = "Register",
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = MaterialTheme.typography.titleLargeEmphasized.fontSize,
                modifier = Modifier.padding(0.dp, 0.dp, 0.dp, 20.dp)
            )

            OutlinedTextField(
                state = vm.usernameState,
                label = { Text("Username") },
            )

            OutlinedTextField(
                state = vm.emailState,
                label = { Text("Email") },
            )

            OutlinedSecureTextField(
                state = vm.passwordState,
                label = { Text("Password") },
            )

            Button(
                onClick = {
                    vm.register()
                }
            ) {
                Text("Register NOW")
            }
        }

    }
}

@Preview
@Composable
fun RegisterViewPreview() {
    RegisterView(
        navController = NavController(androidx.compose.ui.platform.LocalContext.current),
        modifier = Modifier.fillMaxSize()
    )
}

