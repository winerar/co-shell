package app.coshell.core.designsystem.components

import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.material3.Icon
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import app.coshell.core.designsystem.R
import app.coshell.core.designsystem.theme.LocalHeroPalette
import app.coshell.core.designsystem.tokens.Motion
import app.coshell.core.designsystem.tokens.Sizes
import app.coshell.core.designsystem.tokens.Spacing

/**
 * A hero card with a back side (#519, owner 2026-11-19): a tap turns it around the horizontal axis like a real card
 * (`rotationX`, depth from `cameraDistance`, the `Motion.flip` spring with a small overshoot). At 90° the face goes and the
 * back shows; the back is mirrored once more so it reads upright. Both faces stay composed, so the card's height follows
 * the turn from one face's height to the other's without a jump. The hidden face is out of the accessibility tree; the
 * «↻» in the corner is the visible cue and carries [flipLabel] for TalkBack. With system animations off the spring ends at once.
 * The state lives with the caller ([flipped], `rememberSaveable`): a rotation of the phone keeps the side.
 */
@Composable
fun CoShellFlipCard(
    flipped: Boolean,
    onFlip: () -> Unit,
    flipLabel: String,
    modifier: Modifier = Modifier,
    front: @Composable ColumnScope.() -> Unit,
    back: @Composable ColumnScope.() -> Unit,
) {
    val angle = remember { Animatable(if (flipped) HALF_TURN else 0f) }
    LaunchedEffect(flipped) { angle.animateTo(if (flipped) HALF_TURN else 0f, Motion.flip()) }
    val showBack by remember { derivedStateOf { angle.value > QUARTER_TURN } }
    val depth = with(LocalDensity.current) { Sizes.flipDepth.toPx() }
    CoShellCard(
        modifier = modifier.graphicsLayer {
            rotationX = angle.value
            cameraDistance = depth
        },
        tone = CardTone.Hero,
        onClick = onFlip,
    ) {
        Box {
            FlipFaces(progress = { (angle.value / HALF_TURN).coerceIn(0f, 1f) }, showBack = showBack, front = front, back = back)
            Icon(
                painterResource(R.drawable.ic_refresh),
                contentDescription = flipLabel,
                tint = LocalHeroPalette.current.content,
                modifier = Modifier.align(Alignment.TopEnd).padding(Spacing.m).size(Sizes.tileIcon),
            )
        }
    }
}

/** Both faces in one box; the height is read at layout time, so a turn re-measures but never recomposes. */
@Composable
private fun FlipFaces(
    progress: () -> Float,
    showBack: Boolean,
    front: @Composable ColumnScope.() -> Unit,
    back: @Composable ColumnScope.() -> Unit,
) {
    Layout(
        content = {
            Face(visible = !showBack, mirrored = false, content = front)
            Face(visible = showBack, mirrored = true, content = back)
        },
    ) { measurables, constraints ->
        val loose = Constraints(minWidth = constraints.maxWidth, maxWidth = constraints.maxWidth)
        val faces = measurables.map { it.measure(loose) }
        val t = progress()
        val height = (faces[0].height + (faces[1].height - faces[0].height) * t).toInt()
        layout(constraints.maxWidth, height) { faces.forEach { it.place(0, 0) } }
    }
}

@Composable
private fun Face(visible: Boolean, mirrored: Boolean, content: @Composable ColumnScope.() -> Unit) {
    Column(
        Modifier
            .graphicsLayer {
                alpha = if (visible) 1f else 0f
                if (mirrored) rotationX = HALF_TURN
            }
            .then(if (visible) Modifier else Modifier.clearAndSetSemantics {})
            .padding(Spacing.l),
        content = content,
    )
}

/**
 * How far the first line of a face keeps from the right edge so the «↻» in the corner never covers text (the face already
 * has `Spacing.l` padding; the cue sits `Spacing.m` from the card edge). Right-aligned text on the top line pads its end by this.
 */
val FlipCueClearance: Dp = Sizes.tileIcon + Spacing.m - Spacing.l + Spacing.s

private const val HALF_TURN = 180f
private const val QUARTER_TURN = 90f
