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
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.scottmangiapane.open2048.R
import com.scottmangiapane.open2048.model.AppTheme
import com.scottmangiapane.open2048.model.GameMode

@Composable
fun MenuScreen(
    currentTheme: AppTheme,
    onStartGame: (GameMode) -> Unit,
    onResumeGame: () -> Unit,
    onToggleTheme: () -> Unit,
    canResume: Boolean,
) {
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    val amber = colorResource(R.color.amber_500)

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
                    MenuColumn(modifier = Modifier.weight(1f)) {
                        if (canResume) {
                            MenuHeader("CONTINUE")
                            MenuHeaderButton("Resume", onResumeGame, MaterialTheme.colorScheme.secondary)
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                        
                        ChallengeModes(onStartGame, amber)
                    }

                    MenuColumn(modifier = Modifier.weight(1f)) {
                        ClassicModes(onStartGame)
                    }

                    MenuColumn(modifier = Modifier.weight(1f)) {
                        BlitzModes(onStartGame)
                    }
                }
            } else {
                if (canResume) {
                    MenuHeaderButton("Resume Game", onResumeGame, MaterialTheme.colorScheme.secondary)
                    Spacer(modifier = Modifier.height(24.dp))
                }

                ChallengeModes(onStartGame, amber)
                Spacer(modifier = Modifier.height(24.dp))

                ClassicModes(onStartGame)
                Spacer(modifier = Modifier.height(24.dp))
                
                BlitzModes(onStartGame)
            }
        }

        IconButton(
            onClick = onToggleTheme,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .systemBarsPadding()
                .padding(8.dp)
                .zIndex(1f)
        ) {
            val icon = when (currentTheme) {
                AppTheme.LIGHT -> Icons.Default.LightMode
                AppTheme.DARK -> Icons.Default.DarkMode
                AppTheme.CLASSIC -> Icons.Default.Palette
            }
            Icon(
                imageVector = icon,
                contentDescription = "Toggle Theme",
                tint = MaterialTheme.colorScheme.onBackground
            )
        }
    }
}

@Composable
private fun ChallengeModes(onStartGame: (GameMode) -> Unit, amber: Color) {
    MenuHeader("CHALLENGE")
    MenuHeaderButton("Daily Challenge", { onStartGame(GameMode.Daily.today()) }, amber)
}

@Composable
private fun ClassicModes(onStartGame: (GameMode) -> Unit) {
    MenuHeader("CLASSIC")
    MenuHeaderButton("Classic 4x4", { onStartGame(GameMode.Classic(4)) })
    Spacer(modifier = Modifier.height(8.dp))
    MenuHeaderButton("Small 3x3", { onStartGame(GameMode.Classic(3)) })
    Spacer(modifier = Modifier.height(8.dp))
    MenuHeaderButton("Large 5x5", { onStartGame(GameMode.Classic(5)) })
}

@Composable
private fun BlitzModes(onStartGame: (GameMode) -> Unit) {
    MenuHeader("BLITZ")
    MenuHeaderButton("2 Minute Blitz", { onStartGame(GameMode.Blitz(2)) })
    Spacer(modifier = Modifier.height(8.dp))
    MenuHeaderButton("5 Minute Blitz", { onStartGame(GameMode.Blitz(5)) })
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
private fun MenuHeaderButton(
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
