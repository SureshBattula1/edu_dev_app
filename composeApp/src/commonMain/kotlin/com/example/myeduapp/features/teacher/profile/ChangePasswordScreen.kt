package com.example.myeduapp.features.teacher.profile

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.example.myeduapp.core.ui.components.AppLoaderCompact
import com.example.myeduapp.core.ui.theme.PrimaryBlue

class ChangePasswordScreen : Screen {
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val viewModel = rememberScreenModel { ChangePasswordViewModel() }
        val uiState by viewModel.uiState.collectAsState()

        LaunchedEffect(uiState.updateSuccess) {
            if (uiState.updateSuccess) navigator.pop()
        }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Change Password") },
                    navigationIcon = {
                        IconButton(onClick = { navigator.pop() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = PrimaryBlue,
                        titleContentColor = Color.White,
                        navigationIconContentColor = Color.White
                    )
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(24.dp)
            ) {
                OutlinedTextField(
                    value = uiState.oldPassword,
                    onValueChange = viewModel::onOldPasswordChange,
                    label = { Text("Current Password") },
                    modifier = Modifier.fillMaxWidth(),
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                OutlinedTextField(
                    value = uiState.newPassword,
                    onValueChange = viewModel::onNewPasswordChange,
                    label = { Text("New Password (min 6 chars)") },
                    modifier = Modifier.fillMaxWidth(),
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                OutlinedTextField(
                    value = uiState.confirmPassword,
                    onValueChange = viewModel::onConfirmPasswordChange,
                    label = { Text("Confirm New Password") },
                    modifier = Modifier.fillMaxWidth(),
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true,
                    isError = uiState.confirmPassword.isNotEmpty() && uiState.confirmPassword != uiState.newPassword
                )
                
                if (uiState.errorMessage != null) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(uiState.errorMessage!!, color = MaterialTheme.colorScheme.error)
                }
                
                Spacer(modifier = Modifier.height(32.dp))
                
                Button(
                    onClick = viewModel::changePassword,
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    enabled = !uiState.isLoading && uiState.isFormValid
                ) {
                    if (uiState.isLoading) {
                        AppLoaderCompact(size = 28.dp)
                    } else {
                        Text("Update Password")
                    }
                }
            }
        }
    }
}
