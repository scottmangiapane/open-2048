package com.scottmangiapane.open2048.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ScoreCard(label: String, score: Int) {
    val isSmall = DeviceUtils.isSmallScreen()
    val minWidth = if (isSmall) 44.dp else 80.dp
    val hPadding = if (isSmall) 4.dp else 12.dp
    val vPadding = if (isSmall) 4.dp else 8.dp
    val labelStyle = if (isSmall) MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp) else MaterialTheme.typography.labelSmall
    val scoreStyle = if (isSmall) MaterialTheme.typography.titleMedium else MaterialTheme.typography.headlineSmall

    Column(
        modifier = Modifier
            .widthIn(min = minWidth)
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .oledBorder()
            .padding(horizontal = hPadding, vertical = vPadding),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            style = labelStyle,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
            maxLines = 1,
        )
        Text(
            text = score.toString(),
            style = scoreStyle,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
        )
    }
}
