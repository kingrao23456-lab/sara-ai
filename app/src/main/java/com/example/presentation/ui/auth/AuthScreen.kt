package com.example.presentation.ui.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.presentation.viewmodel.AuthViewModel
import com.example.ui.theme.*

@Composable
fun AuthScreen(
    viewModel: AuthViewModel,
    onAuthSuccess: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var isSignUp by remember { mutableStateOf(false) }

    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val isAuthSuccess by viewModel.isAuthSuccess.collectAsState()

    LaunchedEffect(isAuthSuccess) {
        if (isAuthSuccess) {
            viewModel.resetAuthSuccess()
            onAuthSuccess()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AmoledBlack)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            // App Logo
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(NeonPurplePrimary, SurfaceDark)
                        )
                    )
                    .padding(2.dp),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.sara_app_icon_1785575828281),
                    contentDescription = "Sara AI Logo",
                    modifier = Modifier
                        .size(60.dp)
                        .clip(CircleShape)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Welcome to Sara AI",
                color = TextPrimaryDark,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = if (isSignUp) "Create your personal account" else "Sign in to unlock full cloud sync & memory",
                color = TextSecondaryDark,
                fontSize = 13.sp
            )

            Spacer(modifier = Modifier.height(28.dp))

            // Glassmorphism Card
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = CardBackgroundGlass),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, BorderPurpleGlow, RoundedCornerShape(24.dp))
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (errorMessage != null) {
                        Text(
                            text = errorMessage ?: "",
                            color = SoftPinkAccent,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                    }

                    if (isSignUp) {
                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it; viewModel.clearError() },
                            label = { Text("Full Name", color = TextSecondaryDark) },
                            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = NeonPurpleBright) },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = NeonPurpleBright,
                                unfocusedBorderColor = BorderPurpleGlow,
                                focusedTextColor = TextPrimaryDark,
                                unfocusedTextColor = TextPrimaryDark
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("name_input")
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it; viewModel.clearError() },
                        label = { Text("Email Address", color = TextSecondaryDark) },
                        leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = NeonPurpleBright) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonPurpleBright,
                            unfocusedBorderColor = BorderPurpleGlow,
                            focusedTextColor = TextPrimaryDark,
                            unfocusedTextColor = TextPrimaryDark
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("email_input")
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it; viewModel.clearError() },
                        label = { Text("Password", color = TextSecondaryDark) },
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = NeonPurpleBright) },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonPurpleBright,
                            unfocusedBorderColor = BorderPurpleGlow,
                            focusedTextColor = TextPrimaryDark,
                            unfocusedTextColor = TextPrimaryDark
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("password_input")
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = {
                            if (isSignUp) {
                                viewModel.signUp(
                                    email = email,
                                    name = name,
                                    password = password
                                )
                            } else {
                                viewModel.signIn(
                                    email = email,
                                    password = password
                                )
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = NeonPurplePrimary),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("login_button")
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(color = AmoledBlack, modifier = Modifier.size(24.dp))
                        } else {
                            Text(
                                text = if (isSignUp) "Create Account" else "Sign In",
                                color = AmoledBlack,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Google Login Button
                    OutlinedButton(
                        onClick = {
                            val googleEmail = if (email.isNotBlank()) email else "alex.google@sara.ai"
                            val googleName = if (name.isNotBlank()) name else "Alex Google"
                            viewModel.loginWithGoogle(googleEmail, googleName)
                        },
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, BorderPurpleGlow),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("google_login_button")
                    ) {
                        Text("Continue with Google", color = TextPrimaryDark, fontWeight = FontWeight.SemiBold)
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = if (isSignUp) "Already have an account? Sign In" else "Don't have an account? Sign Up",
                        color = NeonPurpleBright,
                        fontSize = 13.sp,
                        modifier = Modifier
                            .clickable { isSignUp = !isSignUp }
                            .padding(4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Continue as Guest Option
            TextButton(
                onClick = {
                    viewModel.loginWithGuest()
                },
                modifier = Modifier.testTag("guest_button")
            ) {
                Text(
                    text = "Continue as Guest →",
                    color = TextSecondaryDark,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
