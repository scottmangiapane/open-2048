package com.scottmangiapane.open2048.ui.components

import androidx.compose.foundation.ScrollState
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

fun Modifier.drawScrollbar(
    state: ScrollState,
    color: Color,
    width: Dp = 4.dp
): Modifier = drawWithContent {
    drawContent()
    if (state.maxValue > 0) {
        val viewPortHeight = size.height
        val contentHeight = viewPortHeight + state.maxValue
        val scrollbarHeight = (viewPortHeight / contentHeight) * viewPortHeight
        val scrollbarTop = (state.value / contentHeight) * viewPortHeight

        drawRect(
            color = color,
            topLeft = Offset(size.width - width.toPx(), scrollbarTop),
            size = Size(width.toPx(), scrollbarHeight)
        )
    }
}
