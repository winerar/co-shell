package app.coshell.recurring.list.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.sp
import app.coshell.R
import app.coshell.core.designsystem.amount.AmountText
import app.coshell.core.designsystem.components.CardTone
import app.coshell.core.designsystem.components.CoShellCard
import app.coshell.core.designsystem.theme.AmountTitleStyle
import app.coshell.core.designsystem.theme.LocalHeroPalette
import app.coshell.core.designsystem.tokens.Spacing
import app.coshell.recurring.list.MonthAheadUi
import app.coshell.ui.format.formatMonthOf

/**
 * The hero of «Регулярные платежи» (board `Rec143List`): «До конца ноября · 9 платежей», the net in the main report
 * currency on the theme gradient, then what it leaves out — payments entered when posting, currencies without a rate.
 */
@Composable
internal fun MonthAheadCard(ahead: MonthAheadUi, modifier: Modifier = Modifier) {
    val month = formatMonthOf(ahead.month)
    CoShellCard(modifier.fillMaxWidth(), tone = CardTone.Hero) {
        Column(Modifier.padding(Spacing.l), verticalArrangement = Arrangement.spacedBy(Spacing.xxs)) {
            Text(
                if (ahead.count == 0) {
                    stringResource(R.string.recurring_ahead_none, month)
                } else {
                    pluralStringResource(R.plurals.recurring_ahead_title, ahead.count, month, ahead.count)
                },
                style = MaterialTheme.typography.labelLarge,
            )
            ahead.total?.let { AmountText(it, style = AmountTitleStyle, color = LocalHeroPalette.current.amount, minFontSize = MinAmount) }
            if (ahead.withoutAmount > 0) Hint(stringResource(R.string.recurring_ahead_without, ahead.withoutAmount))
            if (ahead.missing.isNotEmpty()) Hint(stringResource(R.string.recurring_ahead_missing, ahead.missing.joinToString()))
        }
    }
}

@Composable
private fun Hint(text: String) {
    Text(text, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
}

private val MinAmount = 14.sp
