package app.coshell.reports.components

import app.coshell.ui.motion.Swap
import app.coshell.ui.motion.SwapText
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import app.coshell.R
import app.coshell.core.designsystem.amount.AmountText
import app.coshell.core.designsystem.components.CoShellCard
import app.coshell.core.designsystem.components.CoShellDivider
import app.coshell.core.designsystem.components.CoShellSectionLabel
import app.coshell.core.designsystem.components.CoShellTextButton
import app.coshell.core.designsystem.components.SectionTone
import app.coshell.core.designsystem.theme.AmountTextStyle
import app.coshell.core.designsystem.theme.AmountTitleStyle
import app.coshell.core.designsystem.tokens.Spacing
import app.coshell.reports.Conversion
import app.coshell.reports.ConversionPair

/**
 * «Оплата в другой валюте» (#786, poll 2026-11-27 «А», board `Smart786Card`): the biggest pair — purchases, what was paid and
 * the official price in the same currency, what the conversion cost over the month; «По картам» — which card is cheaper.
 * «Подробнее» — every purchase with its rate ([ConversionSheet]).
 */
@Composable
internal fun ConversionCard(conversion: Conversion, modifier: Modifier = Modifier) {
    var details by rememberSaveable { mutableStateOf(false) }
    val muted = MaterialTheme.colorScheme.onSurfaceVariant
    CoShellCard(modifier.fillMaxWidth()) {
        Column(Modifier.padding(Spacing.l), verticalArrangement = Arrangement.spacedBy(Spacing.s)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    stringResource(R.string.reports_conversion_title),
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.weight(1f).semantics { heading() },
                )
                CoShellTextButton(onClick = { details = true }) { Text(stringResource(R.string.reports_conversion_more)) }
            }
            val pair = conversion.pair
            Line(stringResource(R.string.reports_conversion_purchases, pair.priceCurrency, pair.paidCurrency), pair.price)
            Line(stringResource(R.string.reports_conversion_paid), pair.paid)
            Line(stringResource(R.string.reports_conversion_official), pair.official)
            CoShellDivider()
            Line(
                stringResource(if (conversion.better) R.string.reports_conversion_better else R.string.reports_conversion_cost),
                conversion.total,
                strong = true,
            )
            SwapText(rateLine(pair), style = MaterialTheme.typography.bodySmall, color = muted)
            if (conversion.accounts.size > 1) {
                CoShellSectionLabel(stringResource(R.string.reports_conversion_accounts), tone = SectionTone.Accent, modifier = Modifier.padding(top = Spacing.s))
                conversion.accounts.forEach { account ->
                    Line(
                        stringResource(R.string.reports_conversion_account, account.currency, account.name ?: stringResource(R.string.reports_conversion_gone)),
                        stringResource(if (account.worse) R.string.reports_conversion_worse_short else R.string.reports_conversion_better_short, account.percent),
                    )
                }
            }
            if (conversion.missing.isNotEmpty()) {
                Text(stringResource(R.string.reports_missing, conversion.missing.joinToString()), style = MaterialTheme.typography.bodySmall, color = muted)
            }
        }
    }
    if (details) ConversionSheet(conversion, onClosed = { details = false })
}

/** «средний курс 1 USDT = 997 ARS · официальный 1 015 · хуже на 1,8 %». */
@Composable
private fun rateLine(pair: ConversionPair): String = listOf(
    stringResource(R.string.reports_conversion_rate, pair.rate),
    stringResource(R.string.reports_conversion_official_rate, pair.officialRate),
    stringResource(if (pair.worse) R.string.reports_conversion_worse else R.string.reports_conversion_better_rate, pair.percent),
).joinToString(SEPARATOR)

/** Label and amount; a long label keeps whole words and the amount goes under it (notes-compose #379). */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun Line(label: String, value: String, strong: Boolean = false) {
    FlowRow(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(Spacing.s),
        itemVerticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, style = if (strong) MaterialTheme.typography.titleSmall else MaterialTheme.typography.bodyMedium)
        Row(Modifier.weight(1f), horizontalArrangement = Arrangement.End) {
            Swap(value, contentAlignment = Alignment.TopEnd) { AmountText(it, if (strong) AmountTitleStyle else AmountTextStyle) }
        }
    }
}

private const val SEPARATOR = " · "
