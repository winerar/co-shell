package app.coshell.core.designsystem.motion

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.ContentDrawScope
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.addOutline
import androidx.compose.ui.layout.Measurable
import androidx.compose.ui.layout.MeasureResult
import androidx.compose.ui.layout.MeasureScope
import androidx.compose.ui.node.CompositionLocalConsumerModifierNode
import androidx.compose.ui.node.DrawModifierNode
import androidx.compose.ui.node.LayoutModifierNode
import androidx.compose.ui.node.ModifierNodeElement
import androidx.compose.ui.node.currentValueOf
import androidx.compose.ui.node.invalidateDraw
import androidx.compose.ui.platform.InspectorInfo
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.IntSize
import kotlin.math.max
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

/** Colour of the light a pressed element catches; set by `CoShellTheme` for light/dark. */
val LocalTactileLight = staticCompositionLocalOf { Color.White }

/**
 * Makes an element physical ([Tactile]): put it **first** in the element's modifier chain (so the
 * background and border move too) and give it the element's [interactionSource]. [shape] clips the
 * light to rounded corners. Everything runs in the graphics layer and the draw phase — no
 * recomposition or relayout per frame (performance.md).
 */
fun Modifier.tactile(
    interactionSource: InteractionSource,
    tactile: Tactile = Tactile.Control,
    shape: Shape? = null,
    enabled: Boolean = true,
): Modifier = this then TactileElement(interactionSource, tactile, shape, enabled)

private data class TactileElement(
    val source: InteractionSource,
    val tactile: Tactile,
    val shape: Shape?,
    val enabled: Boolean,
) : ModifierNodeElement<TactileNode>() {
    override fun create() = TactileNode(source, tactile, shape, enabled)

    override fun update(node: TactileNode) = node.update(source, tactile, shape, enabled)

    override fun InspectorInfo.inspectableProperties() {
        name = "tactile"
    }
}

// Press: fast and firm. Release: softer and bouncy — the overshoot is what makes it feel alive.
private val PressSpring = spring<Float>(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMediumLow * 3)
private val ReleaseSpring = spring<Float>(dampingRatio = 0.45f, stiffness = Spring.StiffnessMediumLow)
private val GlowIn = spring<Float>(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMedium)
private val GlowOut = spring<Float>(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessLow)

private class TactileNode(
    private var source: InteractionSource,
    private var tactile: Tactile,
    private var shape: Shape?,
    private var enabled: Boolean,
) : Modifier.Node(), LayoutModifierNode, DrawModifierNode, CompositionLocalConsumerModifierNode {

    private val scale = Animatable(1f)
    private val tiltX = Animatable(0f)
    private val tiltY = Animatable(0f)
    private val glow = Animatable(0f)
    private var glowAt = Offset.Unspecified
    private var size = IntSize.Zero
    private var collector: Job? = null

    override val shouldAutoInvalidate = false

    override fun onAttach() = collect()

    fun update(source: InteractionSource, tactile: Tactile, shape: Shape?, enabled: Boolean) {
        val sourceChanged = source != this.source
        this.source = source
        this.tactile = tactile
        this.shape = shape
        this.enabled = enabled
        if (sourceChanged && isAttached) collect()
        if (!enabled) release()
        invalidateDraw()
    }

    private fun collect() {
        collector?.cancel()
        collector = coroutineScope.launch {
            source.interactions.collect { interaction ->
                when (interaction) {
                    is PressInteraction.Press -> if (enabled) press(interaction.pressPosition)
                    is PressInteraction.Release, is PressInteraction.Cancel -> release()
                }
            }
        }
    }

    private fun press(at: Offset) {
        glowAt = at
        val (x, y) = tiltFor(at.x, at.y, size.width.toFloat(), size.height.toFloat(), tactile.maxTiltDeg)
        coroutineScope.launch { scale.animateTo(tactile.pressedScale, PressSpring) }
        coroutineScope.launch { tiltX.animateTo(x, PressSpring) }
        coroutineScope.launch { tiltY.animateTo(y, PressSpring) }
        coroutineScope.launch { glow.animateTo(1f, GlowIn) { invalidateDraw() } }
    }

    private fun release() {
        if (!isAttached) return
        coroutineScope.launch { scale.animateTo(1f, ReleaseSpring) }
        coroutineScope.launch { tiltX.animateTo(0f, ReleaseSpring) }
        coroutineScope.launch { tiltY.animateTo(0f, ReleaseSpring) }
        coroutineScope.launch { glow.animateTo(0f, GlowOut) { invalidateDraw() } }
    }

    override fun MeasureScope.measure(measurable: Measurable, constraints: Constraints): MeasureResult {
        val placeable = measurable.measure(constraints)
        size = IntSize(placeable.width, placeable.height)
        return layout(placeable.width, placeable.height) {
            // Read inside the layer block: only layer properties update on each frame.
            placeable.placeWithLayer(0, 0) {
                scaleX = scale.value
                scaleY = scale.value
                rotationX = tiltX.value
                rotationY = tiltY.value
                cameraDistance = 12f * density
            }
        }
    }

    override fun ContentDrawScope.draw() {
        drawContent()
        val strength = glow.value * tactile.glow
        if (strength <= 0.001f || glowAt == Offset.Unspecified) return
        val light = currentValueOf(LocalTactileLight)
        val brush = Brush.radialGradient(
            colors = listOf(light.copy(alpha = strength), light.copy(alpha = 0f)),
            center = glowAt,
            radius = max(size.width, size.height) * 0.75f + 1f,
        )
        val outline = shape?.createOutline(this.size, layoutDirection, this)
        if (outline == null) {
            clipRect { drawRect(brush) }
        } else {
            clipPath(Path().apply { addOutline(outline) }) { drawRect(brush) }
        }
    }
}
