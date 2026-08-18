package com.jackattackk246.files.ui.wizard

import android.content.SharedPreferences
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Locale

@Composable
fun SetupWizardOnboardingView(
    onboardingPreferences: SharedPreferences,
    onWizardCompleted: () -> Unit
) {
    // Keep exact dynamic locale setup hooks
    val systemLanguageDetected = remember { 
        Locale.getDefault().displayLanguage.ifBlank { "English" } 
    }
    
    var profileUsernameInput by remember { mutableStateOf("") }
    var selectedLanguageChoice by remember { mutableStateOf(systemLanguageDetected) }
    var selectedRegionChoice by remember { mutableStateOf("") }

    // Master electric blue accent color token
    val electricBlue = Color(0xFF2563EB)

    // Enforce safe drawing layout boundaries
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF121212))
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Main Container Card
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f, fill = false)
                .background(Color(0xFF1E1E1E), RoundedCornerShape(16.dp))
                .border(1.5.dp, electricBlue, RoundedCornerShape(16.dp))
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Welcome Setup",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            // Input Row 1: Username Frame Component
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Username (optional)",
                    color = Color.LightGray,
                    fontSize = 11.sp,
                    modifier = Modifier.padding(bottom = 6.dp)
                )
                OutlinedTextField(
                    value = profileUsernameInput,
                    onValueChange = { profileUsernameInput = it },
                    placeholder = { Text("Name (optional)", color = Color.Gray) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = electricBlue,
                        unfocusedBorderColor = Color.DarkGray
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Input Row 2: Display Language Frame Component
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Display Language",
                    color = Color.LightGray,
                    fontSize = 11.sp,
                    modifier = Modifier.padding(bottom = 6.dp)
                )
                OutlinedTextField(
                    value = selectedLanguageChoice,
                    onValueChange = { selectedLanguageChoice = it },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = electricBlue,
                        unfocusedBorderColor = Color.DarkGray
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Input Row 3: Region Format Frame Component
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Region / Format",
                    color = Color.LightGray,
                    fontSize = 11.sp,
                    modifier = Modifier.padding(bottom = 6.dp)
                )
                OutlinedTextField(
                    value = selectedRegionChoice,
                    onValueChange = { selectedRegionChoice = it },
                    placeholder = { Text("Select Region (optional)", color = Color.Gray) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = electricBlue,
                        unfocusedBorderColor = Color.DarkGray
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Navigation Action Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = { /* Back navigation layout handle */ }) {
                Text("Back", color = Color.White)
            }
            
            Button(
                onClick = {
                    onboardingPreferences.edit().apply {
                        putString("saved_username", profileUsernameInput.trim())
                        putString("saved_language", selectedLanguageChoice)
                        putString("saved_region", selectedRegionChoice)
                        putBoolean("setup_completed", true)
                    }.apply()
                    onWizardCompleted()
                },
                colors = ButtonDefaults.buttonColors(containerColor = electricBlue),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.height(40.dp).width(80.dp)
            ) {
                Text("Next", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
        }
    }
}
