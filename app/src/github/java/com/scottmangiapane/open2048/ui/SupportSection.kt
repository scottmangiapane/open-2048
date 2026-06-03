package com.scottmangiapane.open2048.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalUriHandler
import com.scottmangiapane.open2048.ui.components.GameButton

@Composable
fun SupportSection() {
    val uriHandler = LocalUriHandler.current

    GameButton(
        text = "Support on GitHub Sponsors",
        icon = Icons.Rounded.Favorite,
        onClick = { uriHandler.openUri("https://github.com/sponsors/scottmangiapane") },
        fullWidth = true,
        containerColor = MaterialTheme.colorScheme.surfaceVariant,
        contentColor = MaterialTheme.colorScheme.onSurfaceVariant
    )
}
