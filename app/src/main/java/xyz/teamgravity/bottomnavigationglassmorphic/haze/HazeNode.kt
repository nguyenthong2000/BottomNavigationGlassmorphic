package xyz.teamgravity.bottomnavigationglassmorphic.haze

import android.annotation.SuppressLint
import android.os.Build
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.ContentDrawScope
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.node.CompositionLocalConsumerModifierNode
import androidx.compose.ui.node.DrawModifierNode
import androidx.compose.ui.node.GlobalPositionAwareModifierNode
import androidx.compose.ui.node.currentValueOf
import androidx.compose.ui.platform.LocalGraphicsContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.takeOrElse

@SuppressLint("ExperimentalAnnotationRetention")
@RequiresOptIn(message = "Experimental Haze API", level = RequiresOptIn.Level.WARNING)
annotation class ExperimentalHazeApi

/**
 * The [Modifier.Node] implementation used by [Modifier.haze].
 *
 * This is public API in order to aid custom extensible modifiers, _but_ we reserve the right
 * to be able to change the API in the future, hence why it is marked as experimental forever.
 */
@ExperimentalHazeApi
class HazeNode(
    var state: HazeState,
) : Modifier.Node(),
    CompositionLocalConsumerModifierNode,
    GlobalPositionAwareModifierNode,
    DrawModifierNode {

    override fun onGloballyPositioned(coordinates: LayoutCoordinates) {
        log(TAG) {
            "onGloballyPositioned: " +
                    "positionInWindow=${coordinates.positionInWindow()}, " +
                    "content positionOnScreens=${state.positionOnScreen}"
        }

        state.positionOnScreen = coordinates.positionInWindow() + calculateWindowOffset()
    }

    /**
     * We manually invalidate when things have changed
     */
    override val shouldAutoInvalidate: Boolean = false

    override fun ContentDrawScope.draw() {
        state.contentDrawing = true
        log(TAG) { "start draw()" }

        if (canUseGraphicLayers()) {
            val graphicsContext = currentValueOf(LocalGraphicsContext)

            val contentLayer = state.contentLayer
                ?.takeUnless { it.isReleased }
                ?: graphicsContext.createGraphicsLayer().also { state.contentLayer = it }

            // First we draw the composable content into a graphics layer
            contentLayer.record {
                this@draw.drawContent()
            }

            // Now we draw `content` into the window canvas
            drawLayer(contentLayer)
        } else {
            // If we're not using graphics layers, just call drawContent and return early
            drawContent()
            drawCircle(Color.White.copy(alpha = 0.1f))
        }

        state.contentDrawing = false
        log(TAG) { "end draw()" }
    }

    override fun onDetach() {
        super.onDetach()

        state.contentLayer?.let { layer ->
            currentValueOf(LocalGraphicsContext).releaseGraphicsLayer(layer)
        }
        state.contentLayer = null
    }

    private companion object {
        const val TAG = "HazeNode"
    }
}

internal fun isBlurEnabledByDefault(): Boolean = Build.VERSION.SDK_INT >= 32

/**
 * This is the technical minimum for blurring to work on Android.
 */
internal fun DrawScope.canUseGraphicLayers(): Boolean {
    return Build.VERSION.SDK_INT >= 31 && drawContext.canvas.nativeCanvas.isHardwareAccelerated
}

internal fun HazeTint.boostForFallback(blurRadius: Dp): HazeTint {
    // For color, we can boost the alpha
    val resolved = blurRadius.takeOrElse { HazeDefaults.blurRadius }
    val boosted = color.boostAlphaForBlurRadius(resolved)
    return copy(color = boosted)
}

/**
 * In this implementation, the only tool we have is translucency.
 */
private fun Color.boostAlphaForBlurRadius(blurRadius: Dp): Color {
    // We treat a blur radius of 72.dp as near 'opaque', and linearly boost using that
    val factor = 1 + (blurRadius.value / 72)
    return copy(alpha = (alpha * factor).coerceAtMost(1f))
}