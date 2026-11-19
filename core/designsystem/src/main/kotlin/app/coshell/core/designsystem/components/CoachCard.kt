package app.coshell.core.designsystem.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import app.coshell.core.designsystem.tokens.Sizes
import app.coshell.core.designsystem.tokens.Spacing

/**
 * Gesture hint (#513, owner poll 2026-11-19, board «Компоненты» → «Уведомления»): a card over the bottom bar with a
 * line of text and one text button («Понятно»). Does not point at an element, does not take touches outside itself.
 * Rises like [CoShellNoticePill]; the screen decides when to show it (`app/.../hints/`).
 */
@Composable
fun CoShellCoachCard(
    visible: Boolean,
    text: String,
    actionLabel: String,
    onAction: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AnimatedVisibility(visible = visible, modifier = modifier, enter = PillIn, exit = PillOut) {
        val scheme = MaterialTheme.colorScheme
        val shape = MaterialTheme.shapes.large
        Column(
            Modifier
                .fillMaxWidth()
                .clip(shape)
                .background(scheme.surfaceContainerHigh)
                .border(Sizes.hairline, scheme.outlineVariant, shape)
                .padding(start = Spacing.l, top = Spacing.m, end = Spacing.s, bottom = Spacing.xs),
            verticalArrangement = Arrangement.spacedBy(Spacing.xs),
        ) {
            Text(text, style = MaterialTheme.typography.bodyMedium, color = scheme.onSurface, modifier = Modifier.padding(end = Spacing.m))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End, verticalAlignment = Alignment.CenterVertically) {
                CoShellTextButton(onClick = onAction) { Text(actionLabel, style = MaterialTheme.typography.labelLarge) }
            }
        }
    }
}
