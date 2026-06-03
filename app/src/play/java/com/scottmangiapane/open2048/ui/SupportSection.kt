package com.scottmangiapane.open2048.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import com.scottmangiapane.open2048.ui.components.GameButton

@Composable
fun SupportSection() {
    GameButton(
        text = "Tip the Developer",
        icon = Icons.Rounded.Favorite,
        onClick = { 
            // Launch Google Play Billing Flow
        },
        fullWidth = true,
        containerColor = MaterialTheme.colorScheme.surfaceVariant,
        contentColor = MaterialTheme.colorScheme.onSurfaceVariant
    )
}
