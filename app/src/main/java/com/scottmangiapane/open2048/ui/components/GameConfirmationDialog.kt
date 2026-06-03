package com.scottmangiapane.open2048.ui.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun GameConfirmationDialog(
    title: String,
    message: String,
    confirmText: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onBackground
            )
        },
        text = {
            Text(
                text = message,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f)
            )
        },
        confirmButton = {
            GameButton(
                text = confirmText,
                onClick = onConfirm,
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                padding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                height = 48.dp
            )
        },
        dismissButton = {
            GameButton(
                text = "Cancel",
                onClick = onDismiss,
                containerColor = Color.Transparent,
                contentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                padding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                height = 48.dp
            )
        },
        shape = RoundedCornerShape(8.dp),
        containerColor = MaterialTheme.colorScheme.background,
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp)
    )
}
