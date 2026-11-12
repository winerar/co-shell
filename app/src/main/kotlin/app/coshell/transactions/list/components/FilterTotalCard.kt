package app.coshell.transactions.list.components

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
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import app.coshell.R
import app.coshell.core.designsystem.components.CoShellCard
import app.coshell.core.designsystem.theme.AmountTextStyle
import app.coshell.core.designsystem.theme.LocalAmountPalette
import app.coshell.core.designsystem.theme.RateTextStyle
import app.coshell.core.designsystem.tokens.Spacing
import app.coshell.transactions.list.TransactionsUiState.Kind
import app.coshell.transactions.list.TransactionsUiState.Total

/** Under the filters (4А, board `Idea4A`): «23 операции» on the left, the net and «≈ 182,40 USD» on the right. */
@Composable
internal fun FilterTotalCard(total: Total, modifier: Modifier = Modifier) {
    CoShellCard(modifier.fillMaxWidth()) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = Spacing.m + Spacing.xxs, vertical = Spacing.m),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Spacing.m),
        ) {
            Text(
                pluralStringResource(R.plurals.filter_total_count, total.count, total.count),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
            )
            // The amounts take the rest and wrap there; the count never does.
            Column(Modifier.weight(1f), horizontalAlignment = Alignment.End) {
                total.amount?.let {
                    Text(
                        it,
                        style = AmountTextStyle,
                        color = when (total.tone) {
                            Kind.EXPENSE -> LocalAmountPalette.current.expense
                            Kind.INCOME -> LocalAmountPalette.current.income
                            else -> MaterialTheme.colorScheme.onSurface
                        },
                        maxLines = 1,
                    )
                }
                val detail = listOfNotNull(total.detail, total.noRate?.let { stringResource(R.string.filter_total_no_rate, it) })
                if (detail.isNotEmpty()) {
                    Text(
                        detail.joinToString("\n"),
                        style = RateTextStyle,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.End,
                    )
                }
            }
        }
    }
}
