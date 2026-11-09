package app.coshell.core.designsystem.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import app.coshell.core.designsystem.tokens.Motion
import app.coshell.core.designsystem.tokens.Sizes
import app.coshell.core.designsystem.tokens.Spacing

/** What the ring around a [ProgressHero] shows. */
sealed interface HeroRing {
    /** A thin quiet track: nothing is happening. */
    data object Idle : HeroRing
    /** Spinning arc: working, the amount of work is unknown. */
    data object Busy : HeroRing
    /** Filled arc, [fraction] 0..1 (animated). */
    data class Progress(val fraction: Float) : HeroRing
}

/**
 * The centre piece of a single-purpose screen (update, later import/sync): a big ring with a filled
 * circle inside. When [contentKey] of [state] changes, the [content] is swapped with a scale + fade, so each
 * step "lands" in place; other changes of [state] (a percentage) redraw it in place.
 * [accent] — the circle in the primary container colour (something to act on), otherwise neutral.
 */
@Composable
fun <S> ProgressHero(
    ring: HeroRing,
    state: S,
    modifier: Modifier = Modifier,
    accent: Boolean = false,
    contentKey: (S) -> Any? = { it },
    content: @Composable BoxScope.(S) -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    Box(modifier.size(Sizes.heroRing), contentAlignment = Alignment.Center) {
        val track = colors.outlineVariant
        when (ring) {
            HeroRing.Idle -> CircularProgressIndicator(
                progress = { 0f }, modifier = Modifier.size(Sizes.heroRing),
                strokeWidth = Sizes.heroStroke, trackColor = track,
            )
            HeroRing.Busy -> CircularProgressIndicator(
                modifier = Modifier.size(Sizes.heroRing), strokeWidth = Sizes.heroStroke,
                trackColor = track, strokeCap = StrokeCap.Round,
            )
            is HeroRing.Progress -> {
                val fraction by animateFloatAsState(ring.fraction.coerceIn(0f, 1f), Motion.spatial(), label = "heroProgress")
                CircularProgressIndicator(
                    progress = { fraction }, modifier = Modifier.size(Sizes.heroRing),
                    strokeWidth = Sizes.heroStroke, trackColor = track, strokeCap = StrokeCap.Round,
                )
            }
        }
        Box(
            Modifier
                .matchParentSize()
                .padding(Spacing.l)
                .background(if (accent) colors.primaryContainer else colors.surfaceContainerHigh, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            AnimatedContent(
                targetState = state,
                contentKey = contentKey,
                transitionSpec = {
                    (scaleIn(Motion.spatial(), initialScale = 0.6f) + fadeIn(Motion.effects()))
                        .togetherWith(scaleOut(Motion.spatialFast(), targetScale = 0.6f) + fadeOut(Motion.effectsFast()))
                },
                contentAlignment = Alignment.Center,
                label = "heroContent",
            ) { content(it) }
        }
    }
}
