package xyz.teamgravity.bottomnavigationglassmorphic.haze

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

internal fun HazeProgressive.LinearGradient.asBrush(numStops: Int = 20): Brush {
    return Brush.linearGradient(
        colors = List(numStops) { i ->
            val x = i * 1f / (numStops - 1)
            Color.Magenta.copy(alpha = lerp(startIntensity, endIntensity, easing.transform(x)))
        },
        start = start,
        end = end,
    )
}