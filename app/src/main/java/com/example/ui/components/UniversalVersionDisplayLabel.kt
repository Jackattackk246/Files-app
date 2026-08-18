package com.jackattackk246.files.ui.components

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.Color

@Composable
fun UniversalVersionDisplayLabel() {
    val context = LocalContext.current
    
    // Programmatically query native package flags to pull version variables dynamically
    val displayVersionStr = try {
        val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
        packageInfo.versionName ?: "2.4.6"
    } catch (e: Exception) {
        "2.4.6" // Fallback identifier
    }

    Text(
        text = "Version $displayVersionStr",
        color = Color.Gray,
        fontSize = 12.sp
    )
}
