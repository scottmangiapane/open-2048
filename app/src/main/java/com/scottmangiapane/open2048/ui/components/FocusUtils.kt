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

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.material3.IconButton
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue

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

@Composable
fun MenuIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()
    IconButton(
        onClick = onClick,
        modifier = modifier.appFocusBorder(isFocused = isFocused),
        interactionSource = interactionSource,
        content = content
    )
}
