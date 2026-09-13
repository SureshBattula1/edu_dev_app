package com.example.myeduapp.features.auth.login

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import com.example.myeduapp.core.ui.components.AppButton
import myeduapp.composeapp.generated.resources.Res
import myeduapp.composeapp.generated.resources.login_background
import myeduapp.composeapp.generated.resources.logo_bigbridz
import org.jetbrains.compose.resources.painterResource

class LoginScreen : Screen {
    @Composable
    override fun Content() {
        val viewModel = rememberScreenModel { LoginViewModel() }
        val uiState by viewModel.uiState.collectAsState()

        val fieldShape = RoundedCornerShape(12.dp)
        val fieldColors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = Color.White,
            unfocusedContainerColor = Color.White,
            disabledContainerColor = Color.White,
            focusedBorderColor = Color(0xFF007CC4),
            unfocusedBorderColor = Color(0xFFE2E8F0)
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
        ) {
            Image(
                painter = painterResource(Res.drawable.login_background),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit,
                alignment = Alignment.Center
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Image(
                    painter = painterResource(Res.drawable.logo_bigbridz),
                    contentDescription = "BigBridz",
                    modifier = Modifier
                        .fillMaxWidth(0.55f)
                        .padding(horizontal = 8.dp),
                    contentScale = ContentScale.Fit
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Login to continue",
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(40.dp))

                Surface(
                    modifier = Modifier
                        .fillMaxWidth(0.88f)
                        .shadow(8.dp, fieldShape, clip = false),
                    shape = fieldShape,
                    color = Color.White,
                    tonalElevation = 0.dp,
                    shadowElevation = 0.dp
                ) {
                    OutlinedTextField(
                        value = uiState.identifier,
                        onValueChange = viewModel::onIdentifierChange,
                        label = { Text("Email or Phone") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = fieldShape,
                        colors = fieldColors
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Surface(
                    modifier = Modifier
                        .fillMaxWidth(0.88f)
                        .shadow(8.dp, fieldShape, clip = false),
                    shape = fieldShape,
                    color = Color.White,
                    tonalElevation = 0.dp,
                    shadowElevation = 0.dp
                ) {
                    OutlinedTextField(
                        value = uiState.password,
                        onValueChange = viewModel::onPasswordChange,
                        label = { Text("Password") },
                        modifier = Modifier.fillMaxWidth(),
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true,
                        shape = fieldShape,
                        colors = fieldColors
                    )
                }

                if (uiState.errorMessage != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        uiState.errorMessage!!,
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 14.sp
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                AppButton(
                    text = "Login",
                    onClick = viewModel::login,
                    modifier = Modifier
                        .fillMaxWidth(0.88f)
                        .shadow(6.dp, RoundedCornerShape(14.dp), clip = false),
                    isLoading = uiState.isLoading,
                    enabled = uiState.identifier.isNotBlank() && uiState.password.isNotBlank()
                )
            }
        }
    }
}
