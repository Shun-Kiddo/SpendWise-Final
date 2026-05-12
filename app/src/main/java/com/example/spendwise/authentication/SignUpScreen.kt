package com.example.spendwise.authentication

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.spendwise.ui.theme.BrandColor
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun SignUpScreen(
    onSignUpSuccess: () -> Unit,
    onNavigateToSignIn: () -> Unit
) {
    // State variables
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var work by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Create Account",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Join SpendWise Today",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.secondary
        )

        Spacer(modifier = Modifier.height(32.dp))


        val onValueChangeWrapper: (String, (String) -> Unit) -> Unit = { newValue, setter ->
            setter(newValue)
            if (errorMessage != null) errorMessage = null
        }


        OutlinedTextField(
            value = name,
            onValueChange = { onValueChangeWrapper(it) { name = it } },
            label = { Text("Full Name") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = MaterialTheme.shapes.medium
        )

        Spacer(modifier = Modifier.height(16.dp))


        OutlinedTextField(
            value = phone,
            onValueChange = { input ->
                val filtered = input.filter { it.isDigit() }
                if (filtered.length <= 11) {
                    onValueChangeWrapper(filtered) { phone = it }
                }
            },
            label = { Text("Phone Number (11 digits)") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            shape = MaterialTheme.shapes.medium
        )

        Spacer(modifier = Modifier.height(16.dp))


        OutlinedTextField(
            value = work,
            onValueChange = { onValueChangeWrapper(it) { work = it } },
            label = { Text("Occupation (e.g. Student, Engineer)") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = MaterialTheme.shapes.medium
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Email Field - Optimized for @ sign
        OutlinedTextField(
            value = email,
            onValueChange = { onValueChangeWrapper(it) { email = it } },
            label = { Text("Email Address") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            shape = MaterialTheme.shapes.medium
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Password Field with Toggle Logic
        OutlinedTextField(
            value = password,
            onValueChange = { onValueChangeWrapper(it) { password = it } },
            label = { Text("Password (min. 6 characters)") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            // Logic para sa show/hide password
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            trailingIcon = {
                val image = if (passwordVisible)
                    Icons.Filled.Visibility
                else Icons.Filled.VisibilityOff

                val description = if (passwordVisible) "Hide password" else "Show password"

                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(imageVector = image, contentDescription = description)
                }
            },
            shape = MaterialTheme.shapes.medium
        )

        // Error Message Display
        if (errorMessage != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = errorMessage!!,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.labelMedium
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Sign Up Button using BrandColor
        Button(
            onClick = {
                // Check if all fields are filled
                if (name.isBlank() || email.isBlank() || password.isBlank() || phone.isBlank() || work.isBlank()) {
                    errorMessage = "Please fill in all fields"
                    return@Button
                }


                if (phone.length != 11) {
                    errorMessage = "Phone number must be exactly 11 digits"
                    return@Button
                }

                if (password.length < 6) {
                    errorMessage = "Password must be at least 6 characters"
                    return@Button
                }

                isLoading = true
                errorMessage = null

                auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val user = auth.currentUser
                            val uid = user?.uid ?: ""


                            val profileUpdates = UserProfileChangeRequest.Builder()
                                .setDisplayName(name)
                                .build()

                            user?.updateProfile(profileUpdates)


                            val userData = hashMapOf(
                                "uid" to uid,
                                "name" to name,
                                "phone" to phone,
                                "work" to work,
                                "email" to email
                            )

                            db.collection("users").document(uid)
                                .set(userData)
                                .addOnSuccessListener {
                                    isLoading = false
                                    onSignUpSuccess()
                                }
                                .addOnFailureListener { e ->
                                    isLoading = false
                                    errorMessage = "Failed to save profile: ${e.message}"
                                }

                        } else {
                            isLoading = false
                            errorMessage = task.exception?.localizedMessage ?: "Sign Up failed"
                        }
                    }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            enabled = !isLoading,
            colors = ButtonDefaults.buttonColors(containerColor = BrandColor),
            shape = MaterialTheme.shapes.medium
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 2.dp
                )
            } else {
                Text(
                    "Sign Up",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Navigate to Sign In
        TextButton(onClick = onNavigateToSignIn) {
            Text(
                "Already have an account? Sign In",
                color = BrandColor,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}