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

import androidx.compose.ui.platform.LocalInputModeManager
import androidx.compose.ui.input.InputMode
import androidx.compose.ui.unit.Dp

val Color.isBlack: Boolean
    get() = this == Color.Black || this == Color(0xFF000000)

@Composable
fun Modifier.appFocusBorder(
    isFocused: Boolean,
    shape: Shape = RoundedCornerShape(8.dp),
    color: Color = MaterialTheme.colorScheme.primary
): Modifier {
    val inputMode = LocalInputModeManager.current.inputMode
    return if (isFocused && inputMode == InputMode.Keyboard) {
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
        modifier = modifier
            .appFocusBorder(isFocused = isFocused)
            .then(
                if (MaterialTheme.colorScheme.background.isBlack)
                    Modifier.border(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                else Modifier
            ),
        interactionSource = interactionSource,
        content = content
    )
}

@Composable
fun appContainerColor(): Color {
    return if (MaterialTheme.colorScheme.background.isBlack) Color.Transparent 
           else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
}

@Composable
fun Modifier.oledBorder(
    color: Color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
    shape: Shape = RoundedCornerShape(8.dp),
    borderWidth: Dp = 1.dp
): Modifier {
    return if (MaterialTheme.colorScheme.background.isBlack) {
        this.border(width = borderWidth, color = color, shape = shape)
    } else {
        this
    }
}
