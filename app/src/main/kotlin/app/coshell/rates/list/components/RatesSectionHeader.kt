package app.coshell.rates.list.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import app.coshell.core.designsystem.components.CoShellSectionLabel
import app.coshell.core.designsystem.tokens.Spacing

/** Section heading (К4) with an optional muted hint at its right end: «Валюты ваших счетов · тап — задать вручную». */
@Composable
internal fun RatesSectionHeader(title: String, hint: String? = null) {
    Row(
        Modifier.padding(start = Spacing.l, end = Spacing.l, top = Spacing.l, bottom = Spacing.s),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CoShellSectionLabel(title, Modifier.weight(1f))
        if (hint != null) {
            Text(hint, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
