package de.hsb.vibeify.ui.register

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
fun RegisterView(modifier: Modifier = Modifier, vm: RegisterViewModel = hiltViewModel()) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        val error = vm.uiState.collectAsState().value.generalError
        val hasErrors = vm.uiState.collectAsState().value.hasErrors
        val uiState = vm.uiState.collectAsState().value


        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Register",
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = MaterialTheme.typography.titleLargeEmphasized.fontSize,
                modifier = Modifier.padding(0.dp, 0.dp, 0.dp, 20.dp)
            )


            OutlinedTextField(
                state = vm.emailState,
                label = { Text("Email") },
                isError = uiState.emailError.isNotEmpty(),
                supportingText = {
                    if (uiState.emailError.isNotEmpty()) {
                        Text(uiState.emailError.toString())
                    }
                }
            )

            OutlinedTextField(
                state = vm.confirmEmailState,
                label = { Text("Confirm Email") },
                isError = uiState.confirmEmailError.isNotEmpty(),
                supportingText = {
                    if (uiState.confirmEmailError.isNotEmpty()) {
                        Text(uiState.confirmEmailError.toString())
                    }
                }
            )

            OutlinedSecureTextField(
                state = vm.passwordState,
                label = { Text("Password") },
                isError = uiState.passwordError.isNotEmpty(),
                supportingText = {
                    if (uiState.passwordError.isNotEmpty()) {
                        Text(uiState.passwordError.toString())
                    }
                }
            )

            OutlinedSecureTextField(
                state = vm.confirmPasswordState,
                label = { Text("Confirm Password") },
                isError = uiState.confirmPasswordError.isNotEmpty(),
                supportingText = {
                    if (uiState.confirmPasswordError.isNotEmpty()) {
                        Text(uiState.confirmPasswordError.toString())
                    }
                }
            )

            Button(
                modifier = Modifier.fillMaxWidth(0.5f),
                enabled = !hasErrors,
                onClick = {
                    vm.register()
                }
            ) {
                Text("Register")
            }

        }

    }
}

