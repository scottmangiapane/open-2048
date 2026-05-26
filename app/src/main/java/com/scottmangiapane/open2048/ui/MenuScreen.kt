package com.scottmangiapane.open2048.ui

import android.content.res.Configuration
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
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.scottmangiapane.open2048.model.GameMode
import com.scottmangiapane.open2048.ui.theme.Amber500

@Composable
fun MenuScreen(
    isDarkMode: Boolean,
    onStartGame: (GameMode) -> Unit,
    onResumeGame: () -> Unit,
    onToggleDarkMode: () -> Unit,
    canResume: Boolean
) {
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 32.dp, vertical = if (isLandscape) 16.dp else 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "2048",
                fontSize = if (isLandscape) 56.sp else 64.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(bottom = if (isLandscape) 16.dp else 48.dp)
            )

            if (isLandscape) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(24.dp, Alignment.CenterHorizontally),
                    verticalAlignment = Alignment.Top
                ) {
                    // Group 1: Challenges & Resume
                    MenuColumn(modifier = Modifier.weight(1f)) {
                        if (canResume) {
                            MenuHeader("CONTINUE")
                            MenuButton("Resume", onResumeGame, MaterialTheme.colorScheme.secondary)
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                        
                        MenuHeader("CHALLENGE")
                        MenuButton("Daily Challenge", { onStartGame(GameMode.Daily.today()) }, Amber500)
                    }

                    // Group 2: Classic
                    MenuColumn(modifier = Modifier.weight(1f)) {
                        MenuHeader("CLASSIC")
                        MenuButton("Classic 4x4", { onStartGame(GameMode.Classic(4)) })
                        Spacer(modifier = Modifier.height(8.dp))
                        MenuButton("Small 3x3", { onStartGame(GameMode.Classic(3)) })
                        Spacer(modifier = Modifier.height(8.dp))
                        MenuButton("Large 5x5", { onStartGame(GameMode.Classic(5)) })
                    }

                    // Group 3: Blitz
                    MenuColumn(modifier = Modifier.weight(1f)) {
                        MenuHeader("BLITZ")
                        MenuButton("2m Blitz", { onStartGame(GameMode.Blitz(2)) })
                        Spacer(modifier = Modifier.height(8.dp))
                        MenuButton("5m Blitz", { onStartGame(GameMode.Blitz(5)) })
                    }
                }
            } else {
                if (canResume) {
                    MenuButton("Resume Game", onResumeGame, MaterialTheme.colorScheme.secondary)
                    Spacer(modifier = Modifier.height(24.dp))
                }

                MenuHeader("CHALLENGE")
                MenuButton("Daily Challenge", { onStartGame(GameMode.Daily.today()) }, Amber500)
                Spacer(modifier = Modifier.height(24.dp))

                MenuHeader("CLASSIC")
                MenuButton("Classic 4x4", { onStartGame(GameMode.Classic(4)) })
                Spacer(modifier = Modifier.height(8.dp))
                MenuButton("Small 3x3", { onStartGame(GameMode.Classic(3)) })
                Spacer(modifier = Modifier.height(8.dp))
                MenuButton("Large 5x5", { onStartGame(GameMode.Classic(5)) })
                Spacer(modifier = Modifier.height(24.dp))
                
                MenuHeader("BLITZ")
                MenuButton("2 Minute Blitz", { onStartGame(GameMode.Blitz(2)) })
                Spacer(modifier = Modifier.height(8.dp))
                MenuButton("5 Minute Blitz", { onStartGame(GameMode.Blitz(5)) })
            }
        }

        IconButton(
            onClick = onToggleDarkMode,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .systemBarsPadding()
                .padding(8.dp)
                .zIndex(1f)
        ) {
            Icon(
                imageVector = if (isDarkMode) Icons.Default.LightMode else Icons.Default.DarkMode,
                contentDescription = "Toggle Dark Mode",
                tint = MaterialTheme.colorScheme.onBackground
            )
        }
    }
}

@Composable
private fun MenuColumn(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier.widthIn(max = 240.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        content = content
    )
}

@Composable
private fun MenuHeader(text: String) {
    Text(
        text = text,
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(bottom = 8.dp)
    )
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
        colors = ButtonDefaults.buttonColors(containerColor = containerColor),
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(text, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
    }
}
