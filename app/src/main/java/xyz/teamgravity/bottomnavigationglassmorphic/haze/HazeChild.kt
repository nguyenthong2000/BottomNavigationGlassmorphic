package xyz.teamgravity.bottomnavigationglassmorphic.haze

import androidx.compose.runtime.Stable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.node.ModifierNodeElement
import androidx.compose.ui.platform.InspectorInfo
import androidx.compose.ui.unit.Dp

interface HazeChildScope {

    /**
     * Whether the blur effect is enabled or not, when running on platforms which support blurring.
     *
     * When set to `false` a scrim effect will be used. When set to `true`, and running on a platform
     * which does not support blurring, a scrim effect will be used.
     *
     * Defaults to [HazeDefaults.blurEnabled].
     */
    var blurEnabled: Boolean

    /**
     * The opacity that the overall effect will drawn with, in the range of 0..1.
     */
    var alpha: Float

    /**
     * Style set on this specific [HazeChildNode].
     *
     * There are precedence rules to how each styling property is applied. The order of precedence
     * for each property are as follows:
     *
     *  - Property value set in [HazeChildScope], if specified.
     *  - Value set here in [HazeChildScope.style], if specified.
     *  - Value set in the [LocalHazeStyle] composition local.
     */
    var style: HazeStyle

    /**
     * Optional alpha mask which allows effects such as fading via a
     * [Brush.verticalGradient] or similar. This is only applied when [progressive] is null.
     *
     * An alpha mask provides a similar effect as that provided as [HazeProgressive], in a more
     * performant way, but may provide a less pleasing visual result.
     */
    var mask: Brush?

    /**
     * Color to draw behind the blurred content. Ideally should be opaque
     * so that the original content is not visible behind. Typically this would be
     * `MaterialTheme.colorScheme.surface` or similar.
     *
     * There are precedence rules to how this styling property is applied:
     *
     *  - This property value, if specified.
     *  - [HazeStyle.backgroundColor] value set in [style], if specified.
     *  - [HazeStyle.backgroundColor] value set in the [LocalHazeStyle] composition local.
     */
    var backgroundColor: Color

    /**
     * The [HazeTint]s to apply to the blurred content.
     *
     * There are precedence rules to how this styling property is applied:
     *
     *  - This property value, if not empty.
     *  - [HazeStyle.tints] value set in [style], if not empty.
     *  - [HazeStyle.tints] value set in the [LocalHazeStyle] composition local.
     */
    var tints: List<HazeTint>

    /**
     * Radius of the blur.
     *
     * There are precedence rules to how this styling property is applied:
     *
     *  - This property value, if specified.
     *  - [HazeStyle.blurRadius] value set in [style], if specified.
     *  - [HazeStyle.blurRadius] value set in the [LocalHazeStyle] composition local.
     */
    var blurRadius: Dp

    /**
     * Amount of noise applied to the content, in the range `0f` to `1f`.
     * Anything outside of that range will be clamped.
     *
     * There are precedence rules to how this styling property is applied:
     *
     *  - This property value, if in the range 0f..1f.
     *  - [HazeStyle.noiseFactor] value set in [style], if in the range 0f..1f.
     *  - [HazeStyle.noiseFactor] value set in the [LocalHazeStyle] composition local.
     */
    var noiseFactor: Float

    /**
     * The [HazeTint] to use when Haze uses the fallback scrim functionality.
     *
     * There are precedence rules to how this styling property is applied:
     *
     *  - This property value, if specified
     *  - [HazeStyle.fallbackTint] value set in [style], if specified.
     *  - [HazeStyle.fallbackTint] value set in the [LocalHazeStyle] composition local.
     */
    var fallbackTint: HazeTint

    /**
     * Parameters for enabling a progressive (or gradient) blur effect, or null for a uniform
     * blurring effect. Defaults to null.
     *
     * Please note: progressive blurring effects can be expensive, so you should test on a variety
     * of devices to verify that performance is acceptable for your use case. An alternative and
     * more performant way to achieve this effect is via the [mask] parameter, at the cost of
     * visual finesse.
     */
    var progressive: HazeProgressive?

    /**
     * The input scale factor, which needs to be in the range 0 < x <= 1.
     * Defaults to `1.0`, resulting in no scaling.
     *
     * The content will be scaled by this value in both the x and y dimensions, allowing the blur
     * effect to be potentially applied over scaled-down content, before being scaled back up
     * and drawn at the original size.
     *
     * Using a value less than 1.0 **may** improve performance, at the sacrifice of
     * quality and crispness. As always, run your own benchmarks as to whether this
     * compromise is worth it.
     *
     * If you're looking for a good value to experiment with, `0.8` results in a reduction in
     * total resolution of ~35%, while being visually imperceptible to most people (probably).
     *
     * The minimum value I would realistically use is somewhere in the region of
     * `0.5`, which results in the total pixel count being only 25% of the original content. This
     * will likely be visually perceptible different to no scaling, but depending on the styling
     * parameters will still look pleasing to the user.
     *
     * This feature is experimental as it's unclear how much gain it provides. It may be removed
     * some point in the future.
     */
    @ExperimentalHazeApi
    var inputScale: Float
}

/**
 * Mark this composable as being a Haze child composable.
 *
 * This will update the given [HazeState] whenever the layout is placed, enabling any layouts using
 * [Modifier.haze] to blur any content behind the host composable.
 *
 * @param shape The shape of the content. This will affect the the bounds and outline of
 * the content. Please be aware that using non-rectangular shapes has an effect on performance,
 * since we need to use path clipping.
 * @param style The [HazeStyle] to use on this content. Any specified values in the given
 * style will override that value from the default style, provided to [haze].
 */
@Deprecated(
    message = "Shape clipping is no longer necessary with Haze. You can use `Modifier.clip` or similar.",
    replaceWith = ReplaceWith("clip(shape).hazeChild(state, style)"),
)
fun Modifier.hazeChild(
    state: HazeState,
    shape: Shape,
    style: HazeStyle,
): Modifier = clip(shape).hazeChild(state, style)

/**
 * Mark this composable as being a Haze child composable.
 *
 * This will update the given [HazeState] whenever the layout is placed, enabling any layouts using
 * [Modifier.haze] to blur any content behind the host composable.
 *
 * @param style The [HazeStyle] to use on this content. Any specified values in the given
 * style will override that value from the default style, provided to [haze].
 * @param block block on HazeChildScope where you define the styling and visual properties.
 */
@Stable
fun Modifier.hazeChild(
    state: HazeState,
    style: HazeStyle = HazeStyle.Unspecified,
    block: (HazeChildScope.() -> Unit)? = null,
): Modifier = this then HazeChildNodeElement(state, style, block)

@OptIn(ExperimentalHazeApi::class)
private data class HazeChildNodeElement(
    val state: HazeState,
    val style: HazeStyle = HazeStyle.Unspecified,
    val block: (HazeChildScope.() -> Unit)? = null,
) : ModifierNodeElement<HazeChildNode>() {

    override fun create(): HazeChildNode = HazeChildNode(state, style, block)

    override fun update(node: HazeChildNode) {
        node.state = state
        node.style = style
        node.block = block
        node.update()
    }

    override fun InspectorInfo.inspectableProperties() {
        name = "HazeChild"
    }
}