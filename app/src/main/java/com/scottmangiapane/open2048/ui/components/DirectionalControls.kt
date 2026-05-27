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
        // Vertical-First Layout (Optimized for Landscape height):
        //      [Up]
        // [Left]  [Right]
        //     [Down]
        // Width: 2 buttons, Height: 3 buttons
        Column(
            modifier = modifier,
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ControlButton(
                icon = Icons.Rounded.KeyboardArrowUp,
                onClick = { onMove(Direction.UP) }
            )
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ControlButton(
                    icon = Icons.AutoMirrored.Rounded.KeyboardArrowLeft,
                    onClick = { onMove(Direction.LEFT) }
                )
                ControlButton(
                    icon = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                    onClick = { onMove(Direction.RIGHT) }
                )
            }

            ControlButton(
                icon = Icons.Rounded.KeyboardArrowDown,
                onClick = { onMove(Direction.DOWN) }
            )
        }
    } else {
        // Horizontal-First Layout (Optimized for Portrait width):
        //         [Up]
        // [Left]        [Right]
        //        [Down]
        // Width: 3 buttons, Height: 2 buttons
        Row(
            modifier = modifier,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ControlButton(
                icon = Icons.AutoMirrored.Rounded.KeyboardArrowLeft,
                onClick = { onMove(Direction.LEFT) }
            )
            
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                ControlButton(
                    icon = Icons.Rounded.KeyboardArrowUp,
                    onClick = { onMove(Direction.UP) }
                )
                ControlButton(
                    icon = Icons.Rounded.KeyboardArrowDown,
                    onClick = { onMove(Direction.DOWN) }
                )
            }

            ControlButton(
                icon = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                onClick = { onMove(Direction.RIGHT) }
            )
        }
    }
}
