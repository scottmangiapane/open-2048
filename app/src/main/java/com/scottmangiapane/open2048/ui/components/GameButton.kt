package com.scottmangiapane.open2048.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
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
import androidx.compose.ui.unit.sp

@Composable
fun GameButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    containerColor: Color = MaterialTheme.colorScheme.primaryContainer,
    contentColor: Color = MaterialTheme.colorScheme.onPrimaryContainer,
    padding: PaddingValues = PaddingValues(horizontal = 24.dp, vertical = 8.dp),
    height: Dp = 56.dp,
    enabled: Boolean = true,
    fullWidth: Boolean = false,
    isTextButton: Boolean = false
) {
    val isSmall = DeviceUtils.isSmallScreen()
    val actualHeight = if (isSmall && height == 56.dp) 40.dp else height
    val actualPadding = if (isSmall && padding == PaddingValues(horizontal = 24.dp, vertical = 8.dp)) {
        PaddingValues(horizontal = 10.dp, vertical = 4.dp)
    } else {
        padding
    }
    val iconSize = if (actualHeight < 48.dp) 16.dp else 20.dp
    val textStyle = if (actualHeight < 48.dp) MaterialTheme.typography.labelMedium.copy(fontSize = 11.sp) else MaterialTheme.typography.titleMedium

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
                    modifier = Modifier.size(iconSize)
                )
                if (fullWidth) {
                    Spacer(Modifier.weight(1f))
                } else {
                    Spacer(Modifier.width(if (actualHeight < 48.dp) 6.dp else 12.dp))
                }
            }
            Text(
                text = text,
                style = textStyle,
                textAlign = TextAlign.Center
            )
            if (icon != null && fullWidth) {
                Spacer(Modifier.weight(1f))
                Spacer(Modifier.size(iconSize))
            }
        }
    }

    val finalModifier = modifier
        .then(if (fullWidth) Modifier.fillMaxWidth() else Modifier)
        .height(actualHeight)
        .appFocusBorder(
            isFocused = isFocused,
            color = if (containerColor == MaterialTheme.colorScheme.primaryContainer && !isTextButton) 
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
            contentPadding = actualPadding,
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
            contentPadding = actualPadding,
            modifier = finalModifier,
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp),
            border = if (containerColor.isBlack) 
                BorderStroke(2.dp, contentColor) 
            else null,
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
    val isSmall = DeviceUtils.isSmallScreen()
    val actualHeight = if (isSmall && height == 56.dp) 40.dp else height
    val textStyle = if (actualHeight < 48.dp) MaterialTheme.typography.labelMedium.copy(fontSize = 11.sp) else MaterialTheme.typography.titleMedium

    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    Surface(
        onClick = onClick,
        modifier = modifier
            .height(actualHeight)
            .appFocusBorder(
                isFocused = isFocused,
                color = if (selected) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary
            ),
        interactionSource = interactionSource,
        shape = RoundedCornerShape(8.dp),
        color = if (selected) MaterialTheme.colorScheme.primaryContainer else appContainerColor(),
        contentColor = if (selected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
        border = if (selected && MaterialTheme.colorScheme.primaryContainer.isBlack)
            BorderStroke(2.dp, MaterialTheme.colorScheme.onPrimaryContainer)
        else if (!selected && MaterialTheme.colorScheme.background.isBlack)
            BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f))
        else null
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = text,
                style = textStyle
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
    val isSmall = DeviceUtils.isSmallScreen()
    val buttonSize = if (isSmall) 40.dp else 56.dp
    val iconSize = if (isSmall) 20.dp else 28.dp

    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    FilledIconButton(
        onClick = onClick,
        modifier = modifier
            .size(buttonSize)
            .appFocusBorder(
                isFocused = isFocused,
                color = MaterialTheme.colorScheme.secondary
            )
            .then(
                if (MaterialTheme.colorScheme.primaryContainer.isBlack)
                    Modifier.border(2.dp, MaterialTheme.colorScheme.onPrimaryContainer, RoundedCornerShape(8.dp))
                else Modifier
            ),
        interactionSource = interactionSource,
        colors = IconButtonDefaults.filledIconButtonColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(iconSize)
        )
    }
}
