package com.scottmangiapane.open2048.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp

@Composable
fun Modifier.appFocusBorder(
    isFocused: Boolean,
    shape: Shape = RoundedCornerShape(8.dp),
    color: Color = MaterialTheme.colorScheme.primary
): Modifier {
    return if (isFocused) {
        this.clip(shape)
            .border(width = 3.dp, color = color, shape = shape)
    } else {
        this
    }
}
