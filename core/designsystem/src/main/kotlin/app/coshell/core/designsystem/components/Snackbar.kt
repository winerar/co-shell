package app.coshell.core.designsystem.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarData
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameMillis
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.unit.dp
import app.coshell.core.designsystem.tokens.Motion
import app.coshell.core.designsystem.tokens.Spacing

/**
 * Snackbar host of the app («Операция удалена · Отменить»): the bar rides up from the bottom edge (from
 * under the bottom bar) and back down; a thin line along its bottom shrinks while the offer is open, so it
 * is clear how long «Отменить» stays.
 */
@Composable
fun CoShellSnackbarHost(state: SnackbarHostState, modifier: Modifier = Modifier) {
    AnimatedContent(
        targetState = state.currentSnackbarData,
        modifier = modifier,
        transitionSpec = {
            (slideInVertically(Motion.Placement) { it } + fadeIn(Motion.effects()))
                .togetherWith(slideOutVertically(Motion.Placement) { it } + fadeOut(Motion.effectsFast()))
        },
        contentAlignment = Alignment.BottomCenter,
        label = "snackbar",
    ) { data ->
        if (data != null) CoShellSnackbar(data) else Box(Modifier.fillMaxWidth())
    }
}

@Composable
private fun CoShellSnackbar(data: SnackbarData) {
    val scheme = MaterialTheme.colorScheme
    val left = timeLeft(data)
    val timed = data.visuals.duration != SnackbarDuration.Indefinite
    Snackbar(
        modifier = Modifier.padding(Spacing.m),
        action = data.visuals.actionLabel?.let { label ->
            {
                CoShellTextButton(onClick = data::performAction, contentColor = scheme.inversePrimary) { Text(label) }
            }
        },
    ) {
        Column {
            Text(data.visuals.message)
            if (timed) Box(
                Modifier.fillMaxWidth().padding(top = Spacing.xs).height(TIME_LINE).drawBehind {
                    val w = size.width * left()
                    drawRect(scheme.inversePrimary.copy(alpha = 0.7f), Offset.Zero, Size(w, size.height))
                },
            )
        }
    }
}

/**
 * Share of the offer's time still left (1 → 0), read in the draw phase only. A clock, not a motion: it must
 * run evenly with time, like the timeout it shows.
 */
@Composable
private fun timeLeft(data: SnackbarData): () -> Float {
    var left by remember(data) { mutableFloatStateOf(1f) }
    val total = data.visuals.duration.millis()
    LaunchedEffect(data) {
        if (total == null) return@LaunchedEffect
        val start = withFrameMillis { it }
        while (left > 0f) {
            withFrameMillis { now -> left = timeLeftShare(now - start, total) }
        }
    }
    return { left }
}

/** Share of [totalMs] left after [elapsedMs], clamped to 0…1. Pure. */
internal fun timeLeftShare(elapsedMs: Long, totalMs: Long): Float =
    (1f - elapsedMs.toFloat() / totalMs).coerceIn(0f, 1f)

/** How long Material keeps each snackbar (SnackbarHostState): Short 4 s, Long 10 s, Indefinite — no line. */
private fun SnackbarDuration.millis(): Long? = when (this) {
    SnackbarDuration.Short -> 4_000L
    SnackbarDuration.Long -> 10_000L
    SnackbarDuration.Indefinite -> null
}

private val TIME_LINE = 2.dp
