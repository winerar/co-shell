package app.coshell.core.designsystem.components

import androidx.annotation.DrawableRes

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
 * Hint card (#513, board «Компоненты» → «Уведомления»): a line of text and a text button («Понятно»); with [icon] — a lightbulb
 * in a tinted circle before the text, with [secondaryLabel] — a second action before the first («Открыть»). Since the owner's poll
 * 2026-11-25 «А» every hint lies inside the screen as this card and never leaves by itself (boards `Tip513*`); over the entry sheet
 * it still floats (`SheetCoach`). Rises like [CoShellNoticePill]; the screen decides when to show it (`app/.../hints/`).
 */
@Composable
fun CoShellCoachCard(
    visible: Boolean,
    text: String,
    actionLabel: String,
    onAction: () -> Unit,
    modifier: Modifier = Modifier,
    @DrawableRes icon: Int? = null,
    secondaryLabel: String? = null,
    onSecondary: () -> Unit = {},
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
            Row(horizontalArrangement = Arrangement.spacedBy(Spacing.m), verticalAlignment = Alignment.Top) {
                if (icon != null) MenuIcon(icon)
                Text(text, style = MaterialTheme.typography.bodyMedium, color = scheme.onSurface, modifier = Modifier.weight(1f).padding(end = Spacing.m))
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End, verticalAlignment = Alignment.CenterVertically) {
                if (secondaryLabel != null) CoShellTextButton(onClick = onSecondary) { Text(secondaryLabel, style = MaterialTheme.typography.labelLarge) }
                CoShellTextButton(onClick = onAction) { Text(actionLabel, style = MaterialTheme.typography.labelLarge) }
            }
        }
    }
}
