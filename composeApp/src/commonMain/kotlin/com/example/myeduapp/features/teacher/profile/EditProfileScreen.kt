package com.example.myeduapp.features.teacher.profile

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.example.myeduapp.core.datastore.SessionManager
import com.example.myeduapp.core.datastore.AuthState
import com.example.myeduapp.data.repository.AuthRepository
import com.example.myeduapp.core.ui.components.AppLoaderCompact
import com.example.myeduapp.core.ui.theme.PrimaryBlue
import kotlinx.coroutines.launch

class EditProfileScreen : Screen {
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val authState by SessionManager.authState.collectAsState()
        val user = (authState as? AuthState.Authenticated)?.user ?: return
        val authRepository = remember { AuthRepository() }
        val scope = rememberCoroutineScope()

        var firstName by remember { mutableStateOf(user.first_name) }
        var lastName by remember { mutableStateOf(user.last_name) }
        var phone by remember { mutableStateOf(user.phone ?: "") }
        var isLoading by remember { mutableStateOf(false) }
        var errorMessage by remember { mutableStateOf<String?>(null) }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Edit Profile") },
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
                    value = firstName,
                    onValueChange = { firstName = it },
                    label = { Text("First Name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                OutlinedTextField(
                    value = lastName,
                    onValueChange = { lastName = it },
                    label = { Text("Last Name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Phone Number") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(errorMessage!!, color = MaterialTheme.colorScheme.error)
                }
                
                Spacer(modifier = Modifier.height(32.dp))
                
                Button(
                    onClick = {
                        scope.launch {
                            isLoading = true
                            errorMessage = null
                            val result = authRepository.updateProfile(firstName, lastName, phone.ifBlank { null })
                            isLoading = false
                            if (result.isSuccess) {
                                navigator.pop()
                            } else {
                                errorMessage = result.exceptionOrNull()?.message ?: "Failed to update profile"
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    enabled = !isLoading && firstName.isNotBlank() && lastName.isNotBlank()
                ) {
                    if (isLoading) {
                        AppLoaderCompact(size = 28.dp)
                    } else {
                        Text("Save Changes")
                    }
                }
            }
        }
    }
}
