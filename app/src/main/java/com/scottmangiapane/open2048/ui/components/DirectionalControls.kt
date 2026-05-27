package com.scottmangiapane.open2048.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.scottmangiapane.open2048.logic.Direction

@Composable
fun DirectionalControls(
    isLandscape: Boolean,
    onMove: (Direction) -> Unit,
    modifier: Modifier = Modifier
) {
    if (isLandscape) {
        Column(
            modifier = modifier,
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            ControlButton(Icons.Rounded.KeyboardArrowUp, { onMove(Direction.UP) })
            ControlButton(Icons.AutoMirrored.Rounded.KeyboardArrowLeft, { onMove(Direction.LEFT) })
            ControlButton(Icons.AutoMirrored.Rounded.KeyboardArrowRight, { onMove(Direction.RIGHT) })
            ControlButton(Icons.Rounded.KeyboardArrowDown, { onMove(Direction.DOWN) })
        }
    } else {
        Row(
            modifier = modifier,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ControlButton(Icons.AutoMirrored.Rounded.KeyboardArrowLeft, { onMove(Direction.LEFT) })
            ControlButton(Icons.Rounded.KeyboardArrowUp, { onMove(Direction.UP) })
            ControlButton(Icons.Rounded.KeyboardArrowDown, { onMove(Direction.DOWN) })
            ControlButton(Icons.AutoMirrored.Rounded.KeyboardArrowRight, { onMove(Direction.RIGHT) })
        }
    }
}
