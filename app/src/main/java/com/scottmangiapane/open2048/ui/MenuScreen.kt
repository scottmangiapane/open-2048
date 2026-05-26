package com.scottmangiapane.open2048.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.scottmangiapane.open2048.model.GameMode

@Composable
fun MenuScreen(
    isDarkMode: Boolean,
    onStartGame: (GameMode) -> Unit,
    onResumeGame: () -> Unit,
    onToggleDarkMode: () -> Unit,
    canResume: Boolean
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        IconButton(
            onClick = onToggleDarkMode,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp)
        ) {
            Icon(
                imageVector = if (isDarkMode) Icons.Default.LightMode else Icons.Default.DarkMode,
                contentDescription = "Toggle Dark Mode",
                tint = MaterialTheme.colorScheme.onBackground
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "2048",
                fontSize = 80.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(bottom = 48.dp)
            )

            if (canResume) {
                MenuButton(
                    text = "Resume Game",
                    onClick = onResumeGame,
                    containerColor = MaterialTheme.colorScheme.secondary
                )
                Spacer(modifier = Modifier.height(24.dp))
            }

            Text(
                text = "CLASSIC",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            MenuButton(text = "Classic 4x4", onClick = { onStartGame(GameMode.Classic(4)) })
            Spacer(modifier = Modifier.height(8.dp))
            MenuButton(text = "Small 3x3", onClick = { onStartGame(GameMode.Classic(3)) })
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = "BLITZ",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            MenuButton(text = "2 Minute Blitz", onClick = { onStartGame(GameMode.Blitz(2)) })
            Spacer(modifier = Modifier.height(8.dp))
            MenuButton(text = "5 Minute Blitz", onClick = { onStartGame(GameMode.Blitz(5)) })
        }
    }
}

@Composable
private fun MenuButton(
    text: String,
    onClick: () -> Unit,
    containerColor: Color = MaterialTheme.colorScheme.primary
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(
            text = text,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
    }
}
