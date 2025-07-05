package de.hsb.vibeify.ui.login

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedSecureTextField
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import de.hsb.vibeify.core.navigation.navigateToRegister


@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun LoginView(
    modifier: Modifier = Modifier,
    navController: NavController,
    vm: LoginViewModel = hiltViewModel(),
) {
    val error = vm.uiState.collectAsState().value.error
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(12.dp)

        ) {
            Text(
                text = "Login",
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = MaterialTheme.typography.titleLargeEmphasized.fontSize,
                modifier = Modifier.padding(0.dp, 0.dp, 0.dp, 20.dp)
            )

            OutlinedTextField(
                state = vm.usernameState,
                label = { Text("Username") },
                lineLimits = TextFieldLineLimits.SingleLine,
            )

            OutlinedSecureTextField(
                state = vm.passwordState,
                label = { Text("Password") },

                )
            if (error != null) {
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(top = 12.dp)
                )
            }
            Button(
                modifier = Modifier.fillMaxWidth(0.5f),
                onClick = {
                    vm.signIn()
                }
            ) {
                Text("Login")
            }
            Button(
                modifier = Modifier.fillMaxWidth(0.5f),
                onClick = {
                    navController.navigateToRegister()
                }
            ) {
                Text("Register NOW")
            }
        }
    }

}