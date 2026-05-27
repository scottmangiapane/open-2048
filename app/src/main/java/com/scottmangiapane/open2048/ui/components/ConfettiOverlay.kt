package com.scottmangiapane.open2048.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.withTransform
import kotlinx.coroutines.isActive
import kotlin.random.Random

@Composable
fun ConfettiOverlay(
    modifier: Modifier = Modifier,
    durationMillis: Int = 3000,
    onAnimationFinished: () -> Unit
) {
    val particles = remember {
        List(100) {
            ConfettiParticle(
                color = confettiColors.random(),
                x = Random.nextFloat(),
                y = Random.nextFloat() * -0.5f,
                speed = 0.005f + Random.nextFloat() * 0.01f,
                rotationSpeed = Random.nextFloat() * 10f - 5f,
                horizontalSwing = Random.nextFloat() * 0.005f - 0.0025f,
                size = 10f + Random.nextFloat() * 15f
            )
        }
    }

    var progress by remember { mutableStateOf(0f) }
    
    LaunchedEffect(Unit) {
        val startTime = withFrameNanos { it }
        while (isActive) {
            withFrameNanos { frameTime ->
                val elapsed = (frameTime - startTime) / 1_000_000f
                progress = (elapsed / durationMillis).coerceIn(0f, 1f)
            }
            if (progress >= 1f) {
                onAnimationFinished()
                break
            }
        }
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height
        
        particles.forEach { particle ->
            val currentY = (particle.y + progress * particle.speed * 500f) % 1.5f
            val currentX = particle.x + (progress * particle.horizontalSwing * 1000f) % 1f
            
            if (currentY in 0f..1.1f) {
                withTransform({
                    translate(currentX * width, currentY * height)
                    rotate(progress * 1000f * particle.rotationSpeed)
                }) {
                    drawRect(
                        color = particle.color,
                        size = Size(particle.size, particle.size)
                    )
                }
            }
        }
    }
}

private data class ConfettiParticle(
    val color: Color,
    val x: Float,
    val y: Float,
    val speed: Float,
    val rotationSpeed: Float,
    val horizontalSwing: Float,
    val size: Float
)

private val confettiColors = listOf(
    Color(0xFFF43F5E), // rose-500
    Color(0xFF3B82F6), // royal-blue-500
    Color(0xFF10B981), // emerald-500
    Color(0xFFF59E0B), // amber-500
    Color(0xFF7C3AED), // violet-600
    Color(0xFF0EA5E9), // sky-500
)
