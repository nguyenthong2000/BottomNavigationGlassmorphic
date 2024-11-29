package xyz.teamgravity.bottomnavigationglassmorphic.haze.android

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.BitmapShader
import android.graphics.BlendMode
import android.graphics.BlendModeColorFilter
import android.graphics.Paint
import android.graphics.RenderEffect as AndroidRenderEffect
import android.graphics.RuntimeShader
import android.graphics.Shader
import android.graphics.Shader.TileMode.REPEAT
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.RenderEffect
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.isSpecified
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.node.CompositionLocalConsumerModifierNode
import androidx.compose.ui.node.currentValueOf
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.core.graphics.withScale
import io.github.reactivecircus.cache4k.Cache
import xyz.teamgravity.bottomnavigationglassmorphic.R
import xyz.teamgravity.bottomnavigationglassmorphic.haze.BLUR_SKSL
import xyz.teamgravity.bottomnavigationglassmorphic.haze.HazeTint
import xyz.teamgravity.bottomnavigationglassmorphic.haze.RenderEffectParams
import kotlin.concurrent.getOrSet
import kotlin.math.roundToInt

private val noiseTextureCache by lazy {
    Cache.Builder<Pair<Int, Int>, Bitmap>()
        .maximumCacheSize(3)
        .build()
}

private fun Context.getNoiseTexture(noiseFactor: Float, scale: Float): Bitmap {
    val noiseAlphaInt = (noiseFactor * 255).roundToInt().coerceIn(0, 255)
    val scaleInt = (scale * 100).roundToInt().coerceIn(0, 100)
    val key = noiseAlphaInt to scaleInt
    val cached = noiseTextureCache.get(key)
    if (cached != null && !cached.isRecycled) {
        return cached
    }

    // We draw the noise with the given opacity
    return BitmapFactory.decodeResource(resources, R.drawable.haze_noise)
        .transform(alpha = noiseAlphaInt, scale = scale)
        .also { noiseTextureCache.put(key, it) }
}

@RequiresApi(31)
fun AndroidRenderEffect.withNoise(
    context: Context,
    noiseFactor: Float,
    scaleFactor: Float = 1f,
): AndroidRenderEffect = when {
    noiseFactor >= 0.005f -> {
        val noiseShader =
            BitmapShader(context.getNoiseTexture(noiseFactor, scaleFactor), REPEAT, REPEAT)
        AndroidRenderEffect.createBlendModeEffect(
            AndroidRenderEffect.createShaderEffect(noiseShader), // dst
            this, // src
            BlendMode.DST_ATOP, // blendMode
        )
    }

    else -> this
}

@RequiresApi(31)
 fun AndroidRenderEffect.withMask(
    mask: Brush?,
    size: Size,
    offset: Offset,
    blendMode: BlendMode = BlendMode.DST_IN,
): AndroidRenderEffect {
    val shader = mask?.toShader(size) ?: return this
    return blendWith(AndroidRenderEffect.createShaderEffect(shader), blendMode, offset)
}

fun Brush.toShader(size: Size): Shader? = when (this) {
    is ShaderBrush -> createShader(size)
    else -> null
}

@RequiresApi(31)
 fun AndroidRenderEffect.withTints(
    tints: List<HazeTint>,
    alphaModulate: Float = 1f,
    mask: Shader? = null,
    maskOffset: Offset = Offset.Zero,
): AndroidRenderEffect = tints.fold(this) { acc, tint ->
    acc.withTint(tint, alphaModulate, mask, maskOffset)
}

@RequiresApi(31)
private fun AndroidRenderEffect.withTint(
    tint: HazeTint,
    alphaModulate: Float = 1f,
    mask: Shader?,
    maskOffset: Offset,
): AndroidRenderEffect {
    if (!tint.color.isSpecified) return this
    val color = when {
        alphaModulate < 1f -> tint.color.copy(alpha = tint.color.alpha * alphaModulate)
        else -> tint.color
    }
    if (color.alpha < 0.005f) return this

    return if (mask != null) {
        blendWith(
            foreground = AndroidRenderEffect.createColorFilterEffect(
                BlendModeColorFilter(color.toArgb(), BlendMode.SRC_IN),
                AndroidRenderEffect.createShaderEffect(mask),
            ),
            blendMode = tint.blendMode.toAndroidBlendMode(),
            offset = maskOffset,
        )
    } else {
        AndroidRenderEffect.createColorFilterEffect(
            BlendModeColorFilter(color.toArgb(), tint.blendMode.toAndroidBlendMode()),
            this,
        )
    }
}

@RequiresApi(31)
private fun AndroidRenderEffect.blendWith(
    foreground: AndroidRenderEffect,
    blendMode: BlendMode,
    offset: Offset = Offset.Zero,
): AndroidRenderEffect = AndroidRenderEffect.createBlendModeEffect(
    this,
    // We need to offset the shader to the bounds
    AndroidRenderEffect.createOffsetEffect(offset.x, offset.y, foreground),
    blendMode,
)

@RequiresApi(33)
fun createBlurImageFilterWithMask(
    blurRadiusPx: Float,
    bounds: Rect,
    mask: Shader,
): AndroidRenderEffect {
    fun shader(vertical: Boolean): AndroidRenderEffect {
        val shader = RuntimeShader(BLUR_SKSL).apply {
            setFloatUniform("blurRadius", blurRadiusPx)
            setIntUniform("direction", if (vertical) 1 else 0)
            setFloatUniform("crop", bounds.left, bounds.top, bounds.right, bounds.bottom)
            setInputShader("mask", mask)
        }
        return AndroidRenderEffect.createRuntimeShaderEffect(shader, "content")
    }

    // Our blur runtime shader is separated, therefore requires two passes, one in each direction
    return shader(vertical = false).chainWith(shader(vertical = true))
}

@RequiresApi(31)
private fun AndroidRenderEffect.chainWith(imageFilter: AndroidRenderEffect): AndroidRenderEffect {
    return AndroidRenderEffect.createChainEffect(imageFilter, this)
}

/**
 * Returns a copy of the current [Bitmap], drawn with the given [alpha] value.
 *
 * There might be a better way to do this via a [BlendMode], but none of the results looked as
 * good.
 */
private fun Bitmap.transform(alpha: Int, scale: Float): Bitmap {
    val paint = paintLocal.getOrSet { Paint() }
    paint.reset()
    paint.alpha = alpha

    val bitmap = Bitmap.createBitmap(
        (width * scale).toInt(),
        (height * scale).toInt(),
        Bitmap.Config.ARGB_8888,
    )
    android.graphics.Canvas(bitmap).apply {
        withScale(scale, scale) {
            drawBitmap(this@transform, 0f, 0f, paint)
        }
    }
    return bitmap
}

private val paintLocal = ThreadLocal<Paint>()