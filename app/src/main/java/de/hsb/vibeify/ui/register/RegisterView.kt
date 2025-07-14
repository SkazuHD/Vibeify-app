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
import androidx.compose.ui.focus.onFocusChanged
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
        // Collect state flows from ViewModel
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


            // Email input with error handling and focus tracking
            OutlinedTextField(
                state = vm.emailState,
                label = { Text("Email") },
                isError = uiState.emailError.isNotEmpty() && vm.emailTouched.value,
                supportingText = {
                    if (uiState.emailError.isNotEmpty() && vm.emailTouched.value) {
                        Text(uiState.emailError)
                    }
                },
                modifier = Modifier.onFocusChanged { focusState ->
                    if (focusState.isFocused) {
                        vm.emailHadFocus.value = true
                    } else if (vm.emailHadFocus.value) {
                        vm.emailTouched.value = true
                    }
                }
            )

            // Confirm Email input, same validation approach
            OutlinedTextField(
                state = vm.confirmEmailState,
                label = { Text("Confirm Email") },
                isError = uiState.confirmEmailError.isNotEmpty() && vm.confirmEmailTouched.value,
                supportingText = {
                    if (uiState.confirmEmailError.isNotEmpty() && vm.confirmEmailTouched.value) {
                        Text(uiState.confirmEmailError)
                    }
                },
                modifier = Modifier.onFocusChanged { focusState ->
                    if (focusState.isFocused) {
                        vm.confirmEmailHadFocus.value = true
                    } else if (vm.confirmEmailHadFocus.value) {
                        vm.confirmEmailTouched.value = true
                    }
                }
            )

            // Password input with secure text and validation
            OutlinedSecureTextField(
                state = vm.passwordState,
                label = { Text("Password") },
                isError = uiState.passwordError.isNotEmpty() && vm.passwordTouched.value,
                supportingText = {
                    if (uiState.passwordError.isNotEmpty() && vm.passwordTouched.value) {
                        Text(uiState.passwordError)
                    }
                },
                modifier = Modifier.onFocusChanged { focusState ->
                    if (focusState.isFocused) {
                        vm.passwordHadFocus.value = true
                    } else if (vm.passwordHadFocus.value) {
                        vm.passwordTouched.value = true
                    }
                }
            )

            // Confirm Password input, same as above
            OutlinedSecureTextField(
                state = vm.confirmPasswordState,
                label = { Text("Confirm Password") },
                isError = uiState.confirmPasswordError.isNotEmpty() && vm.confirmPasswordTouched.value,
                supportingText = {
                    if (uiState.confirmPasswordError.isNotEmpty() && vm.confirmPasswordTouched.value) {
                        Text(uiState.confirmPasswordError)
                    }
                },
                modifier = Modifier.onFocusChanged { focusState ->
                    if (focusState.isFocused) {
                        vm.confirmPasswordHadFocus.value = true
                    } else if (vm.confirmPasswordHadFocus.value) {
                        vm.confirmPasswordTouched.value = true
                    }
                }
            )

            // Submit button enabled only if no validation errors present
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
