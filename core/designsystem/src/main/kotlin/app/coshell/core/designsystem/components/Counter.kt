package app.coshell.core.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.disabled
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import app.coshell.core.designsystem.R
import app.coshell.core.designsystem.motion.Tactile
import app.coshell.core.designsystem.motion.tactile
import app.coshell.core.designsystem.theme.AmountTitleStyle
import app.coshell.core.designsystem.tokens.Sizes
import app.coshell.core.designsystem.tokens.Spacing
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Counter «− value +» (#777, poll 2026-11-27 «А», board `CompChoice` → «Счётчик»): round 48 dp buttons on the filled track, the value
 * in the middle with an optional [caption] under it. Holding a button keeps stepping, faster after a moment. [onStep] gets −1 or +1;
 * the caller decides the range and turns a button off with [canMinus]/[canPlus]. [mono] — digits («9:30») in the amount font.
 */
@Composable
fun CoShellCounter(
    value: String,
    onStep: (Int) -> Unit,
    minusLabel: String,
    plusLabel: String,
    modifier: Modifier = Modifier,
    caption: String? = null,
    canMinus: Boolean = true,
    canPlus: Boolean = true,
    mono: Boolean = false,
) {
    Row(modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(Spacing.m)) {
        StepButton(R.drawable.ic_remove, minusLabel, canMinus) { onStep(-1) }
        Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                value,
                style = if (mono) AmountTitleStyle else MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
                maxLines = 1,
            )
            if (caption != null) {
                Text(caption, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
            }
        }
        StepButton(R.drawable.ic_add, plusLabel, canPlus) { onStep(1) }
    }
}

/** One round button: a press steps once, holding repeats ([REPEAT_AFTER_MS], then every [REPEAT_EVERY_MS] down to [REPEAT_FAST_MS]). */
@Composable
private fun StepButton(icon: Int, label: String, enabled: Boolean, onStep: () -> Unit) {
    val source = remember { MutableInteractionSource() }
    val scope = rememberCoroutineScope()
    val step by rememberUpdatedState(onStep)
    val on by rememberUpdatedState(enabled)
    Box(
        Modifier
            .size(Sizes.button)
            .tactile(source, Tactile.Control, CircleShape, enabled)
            .background(MaterialTheme.colorScheme.surfaceContainerHigh, CircleShape)
            .alpha(if (enabled) 1f else DisabledAlpha)
            .semantics {
                role = Role.Button
                contentDescription = label
                if (!enabled) disabled()
                onClick { if (on) step(); on }
            }
            .pointerInput(Unit) {
                awaitEachGesture {
                    val down = awaitFirstDown()
                    if (!on) return@awaitEachGesture
                    val press = PressInteraction.Press(down.position)
                    source.tryEmit(press)
                    step()
                    val repeat: Job = scope.launch {
                        delay(REPEAT_AFTER_MS)
                        var every = REPEAT_EVERY_MS
                        while (on) {
                            step()
                            delay(every)
                            every = (every * 4 / 5).coerceAtLeast(REPEAT_FAST_MS)
                        }
                    }
                    val up = waitForUpOrCancellation()
                    repeat.cancel()
                    source.tryEmit(if (up != null) PressInteraction.Release(press) else PressInteraction.Cancel(press))
                }
            },
        contentAlignment = Alignment.Center,
    ) {
        Icon(painterResource(icon), contentDescription = null, tint = MaterialTheme.colorScheme.onSurface)
    }
}

private const val REPEAT_AFTER_MS = 450L
private const val REPEAT_EVERY_MS = 160L
private const val REPEAT_FAST_MS = 50L
private const val DisabledAlpha = 0.38f
