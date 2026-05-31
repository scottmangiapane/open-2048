package com.scottmangiapane.open2048.ui.components

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun GameButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    containerColor: Color = MaterialTheme.colorScheme.primary,
    contentColor: Color = Color.White,
    padding: PaddingValues = PaddingValues(horizontal = 24.dp, vertical = 8.dp),
    height: Dp = 56.dp,
    enabled: Boolean = true,
    fullWidth: Boolean = false,
    isTextButton: Boolean = false
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    val buttonColors = if (isTextButton) {
        ButtonDefaults.textButtonColors(contentColor = containerColor)
    } else {
        ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor,
            disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
            disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }

    val content = @Composable {
        Row(
            modifier = if (fullWidth) Modifier.fillMaxWidth() else Modifier,
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                if (fullWidth) {
                    Spacer(Modifier.weight(1f))
                } else {
                    Spacer(Modifier.width(12.dp))
                }
            }
            Text(
                text = text,
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center
            )
            if (icon != null && fullWidth) {
                Spacer(Modifier.weight(1f))
                Spacer(Modifier.size(20.dp))
            }
        }
    }

    val finalModifier = modifier
        .then(if (fullWidth) Modifier.fillMaxWidth() else Modifier)
        .height(height)
        .appFocusBorder(
            isFocused = isFocused,
            color = if (containerColor == MaterialTheme.colorScheme.primary && !isTextButton) 
                MaterialTheme.colorScheme.secondary 
            else MaterialTheme.colorScheme.primary
        )

    if (isTextButton) {
        TextButton(
            onClick = onClick,
            enabled = enabled,
            interactionSource = interactionSource,
            colors = buttonColors,
            shape = RoundedCornerShape(8.dp),
            contentPadding = padding,
            modifier = finalModifier,
            content = { content() }
        )
    } else {
        Button(
            onClick = onClick,
            enabled = enabled,
            interactionSource = interactionSource,
            colors = buttonColors,
            shape = RoundedCornerShape(8.dp),
            contentPadding = padding,
            modifier = finalModifier,
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp),
            content = { content() }
        )
    }
}

@Composable
fun SelectableButton(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    height: Dp = 56.dp
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    Surface(
        onClick = onClick,
        modifier = modifier
            .height(height)
            .appFocusBorder(
                isFocused = isFocused,
                color = if (selected) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary
            ),
        interactionSource = interactionSource,
        shape = RoundedCornerShape(8.dp),
        color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f),
        contentColor = if (selected) Color.White else MaterialTheme.colorScheme.onSurface
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = text,
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}

@Composable
fun ControlButton(
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    FilledIconButton(
        onClick = onClick,
        modifier = modifier
            .size(56.dp)
            .appFocusBorder(
                isFocused = isFocused,
                color = MaterialTheme.colorScheme.secondary
            ),
        interactionSource = interactionSource,
        colors = IconButtonDefaults.filledIconButtonColors(
            containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(28.dp)
        )
    }
}
