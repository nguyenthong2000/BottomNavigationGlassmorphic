package xyz.teamgravity.bottomnavigationglassmorphic.haze.android

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.layer.GraphicsLayer
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.node.currentValueOf
import androidx.compose.ui.platform.LocalGraphicsContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.takeOrElse
import xyz.teamgravity.bottomnavigationglassmorphic.haze.ExperimentalHazeApi
import xyz.teamgravity.bottomnavigationglassmorphic.haze.HazeChildNode
import xyz.teamgravity.bottomnavigationglassmorphic.haze.HazeChildNode.Companion.TAG
import xyz.teamgravity.bottomnavigationglassmorphic.haze.HazeProgressive
import xyz.teamgravity.bottomnavigationglassmorphic.haze.asBrush
import xyz.teamgravity.bottomnavigationglassmorphic.haze.calculateLength
import xyz.teamgravity.bottomnavigationglassmorphic.haze.getOrCreateRenderEffect
import xyz.teamgravity.bottomnavigationglassmorphic.haze.lerp
import xyz.teamgravity.bottomnavigationglassmorphic.haze.log
import xyz.teamgravity.bottomnavigationglassmorphic.haze.resolveBlurRadius
import xyz.teamgravity.bottomnavigationglassmorphic.haze.resolveNoiseFactor
import xyz.teamgravity.bottomnavigationglassmorphic.haze.resolveTints
import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.min

private const val USE_RUNTIME_SHADER = true

@OptIn(ExperimentalHazeApi::class)
@RequiresApi(31)
internal fun HazeChildNode.drawLinearGradientProgressiveEffect(
    drawScope: DrawScope,
    progressive: HazeProgressive.LinearGradient,
    contentLayer: GraphicsLayer,
) {
    if (USE_RUNTIME_SHADER && Build.VERSION.SDK_INT >= 33) {
        with(drawScope) {
            contentLayer.renderEffect = getOrCreateRenderEffect(progressive = progressive.asBrush())
            contentLayer.alpha = alpha

            // Finally draw the layer
            drawLayer(contentLayer)
        }
    } else if (progressive.preferPerformance) {
        // When the 'prefer performance' flag is enabled, we switch to using a mask instead
        with(drawScope) {
            contentLayer.renderEffect = getOrCreateRenderEffect(mask = progressive.asBrush())
            contentLayer.alpha = alpha

            // Finally draw the layer
            drawLayer(contentLayer)
        }
    } else {
        drawLinearGradientProgressiveEffectUsingLayers(
            drawScope = drawScope,
            progressive = progressive,
            contentLayer = contentLayer,
        )
    }
}

@OptIn(ExperimentalHazeApi::class)
private fun HazeChildNode.drawLinearGradientProgressiveEffectUsingLayers(
    drawScope: DrawScope,
    progressive: HazeProgressive.LinearGradient,
    contentLayer: GraphicsLayer,
) = with(drawScope) {
    require(progressive.startIntensity in 0f..1f)
    require(progressive.endIntensity in 0f..1f)

    // Here we're going to calculate an appropriate amount of steps for the length.
    // We use a calculation of 60dp per step, which is a good balance between
    // quality vs performance
    val stepHeightPx = with(drawContext.density) { 60.dp.toPx() }
    val length = calculateLength(progressive.start, progressive.end, size)
    val steps = ceil(length / stepHeightPx).toInt().coerceAtLeast(2)

    val graphicsContext = currentValueOf(LocalGraphicsContext)

    val seq = when {
        progressive.endIntensity >= progressive.startIntensity -> 0..steps
        else -> steps downTo 0
    }

    val tints = resolveTints()
    val noiseFactor = resolveNoiseFactor()
    val blurRadius = resolveBlurRadius().takeOrElse { 0.dp } * inputScale

    for (i in seq) {
        val fraction = i / steps.toFloat()
        val intensity = lerp(
            progressive.startIntensity,
            progressive.endIntensity,
            progressive.easing.transform(fraction),
        )

        val layer = graphicsContext.createGraphicsLayer()
        layer.record(contentLayer.size) {
            drawLayer(contentLayer)
        }

        log(TAG) {
            "drawProgressiveEffect. " +
                    "step=$i, " +
                    "fraction=$fraction, " +
                    "intensity=$intensity"
        }

        val min = min(progressive.startIntensity, progressive.endIntensity)
        val max = max(progressive.startIntensity, progressive.endIntensity)

        layer.renderEffect = getOrCreateRenderEffect(
            blurRadius = blurRadius * intensity,
            noiseFactor = noiseFactor,
            tints = tints,
            tintAlphaModulate = intensity,
            mask = Brush.linearGradient(
                lerp(min, max, (i - 2f) / steps) to Color.Transparent,
                lerp(min, max, (i - 1f) / steps) to Color.Black,
                lerp(min, max, (i + 0f) / steps) to Color.Black,
                lerp(min, max, (i + 1f) / steps) to Color.Transparent,
                start = progressive.start,
                end = progressive.end,
            ),
        )
        layer.alpha = alpha

        // Since we included a border around the content, we need to translate so that
        // we don't see it (but it still affects the RenderEffect)
        drawLayer(layer)

        graphicsContext.releaseGraphicsLayer(layer)
    }
}