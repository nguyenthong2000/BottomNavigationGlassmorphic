package xyz.teamgravity.bottomnavigationglassmorphic.haze

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.isFinite
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.translate

internal inline fun DrawScope.translate(
    offset: Offset,
    block: DrawScope.() -> Unit,
) {
    if (offset.isFinite && offset != Offset.Zero) {
        translate(offset.x, offset.y, block)
    } else {
        block()
    }
}