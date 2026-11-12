package app.coshell.currencies.list.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.sp
import app.coshell.R
import app.coshell.core.designsystem.amount.AmountText
import app.coshell.core.designsystem.components.CoShellCard
import app.coshell.core.designsystem.theme.AmountHeroStyle
import app.coshell.core.designsystem.theme.LocalHeroPalette
import app.coshell.core.designsystem.tokens.Spacing

/**
 * Board `ReportCurrencies`: «Общий баланс · так будет на «Обзоре»» — a hero on the theme gradient with the
 * total in the main currency, the other report currencies in one grey line. Follows taps on the rows below.
 */
@Composable
internal fun BalancePreviewCard(totals: List<String>, modifier: Modifier = Modifier) {
    val main = totals.firstOrNull() ?: return
    CoShellCard(modifier.fillMaxWidth().padding(horizontal = Spacing.l, vertical = Spacing.s), accent = true) {
        Column(Modifier.padding(Spacing.l)) {
            Text(stringResource(R.string.report_currencies_preview), style = MaterialTheme.typography.labelLarge)
            AmountText(text = main, style = AmountHeroStyle, color = LocalHeroPalette.current.amount, minFontSize = MinAmount)
            if (totals.size > 1) {
                Text(
                    totals.drop(1).joinToString(" · "),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

private val MinAmount = 16.sp
