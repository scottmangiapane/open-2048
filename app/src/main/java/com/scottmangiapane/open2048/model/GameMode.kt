package com.scottmangiapane.open2048.model

import androidx.compose.runtime.saveable.Saver
import java.util.Calendar

sealed class GameMode {
    abstract val size: Int
    abstract val id: String

    data class Classic(override val size: Int) : GameMode() {
        override val id: String = "classic_$size"
    }

    data class Blitz(val durationMinutes: Int) : GameMode() {
        override val size: Int = 4
        override val id: String = "blitz_$durationMinutes"
    }

    data class Daily(val year: Int, val month: Int, val day: Int) : GameMode() {
        override val size: Int = 4
        override val id: String = "daily_${year}_${month}_${day}"
        
        val dateSeed: Long get() = (year * 10000 + month * 100 + day).toLong()
        
        companion object {
            fun today(): Daily {
                val cal = Calendar.getInstance()
                return Daily(
                    cal.get(Calendar.YEAR),
                    cal.get(Calendar.MONTH) + 1,
                    cal.get(Calendar.DAY_OF_MONTH)
                )
            }
        }
    }

    companion object {
        val Saver: Saver<GameMode?, String> = Saver(
            save = { it?.id ?: "" },
            restore = { id ->
                val parts = id.split("_")
                when (parts.getOrNull(0)) {
                    "classic" -> parts.getOrNull(1)?.toIntOrNull()?.let { Classic(it) }
                    "blitz" -> parts.getOrNull(1)?.toIntOrNull()?.let { Blitz(it) }
                    "daily" -> {
                        val y = parts.getOrNull(1)?.toIntOrNull() ?: return@Saver null
                        val m = parts.getOrNull(2)?.toIntOrNull() ?: return@Saver null
                        val d = parts.getOrNull(3)?.toIntOrNull() ?: return@Saver null
                        Daily(y, m, d)
                    }
                    else -> null
                }
            }
        )
    }
}
