package app.coshell.rates.list.components

import app.coshell.core.designsystem.components.CardTone
import app.coshell.core.designsystem.components.StatusTone
import app.coshell.core.designsystem.components.CoShellStatusChip
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import app.coshell.R
import app.coshell.core.designsystem.components.CoShellCard
import app.coshell.core.designsystem.components.CoShellIconButton
import app.coshell.core.designsystem.theme.RateTextStyle
import app.coshell.core.designsystem.tokens.Spacing
import app.coshell.rates.list.ManualRateItem
import app.coshell.rates.list.formatRateDate
import app.coshell.core.designsystem.R as DesignR

/**
 * A manual rate as a violet card (board `Rates`, poll #367 А): «1 USD = 1 450 ARS»,
 * the label «вручную» and «с 5 ноября»; tap edits, the bin deletes.
 */
@Composable
internal fun ManualRateRow(item: ManualRateItem, onClick: () -> Unit, onDelete: () -> Unit) {
    val rate = item.rate
    CoShellCard(
        Modifier.fillMaxWidth().padding(horizontal = Spacing.l, vertical = Spacing.xs),
        tone = CardTone.Secondary,
        onClick = onClick,
    ) {
        Row(
            Modifier.height(IntrinsicSize.Min).padding(start = Spacing.m, top = Spacing.m, bottom = Spacing.m),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Spacing.m),
        ) {
            Column(Modifier.weight(1f)) {
                Text(stringResource(R.string.rates_line, rate.from.value, item.rateText, rate.to.value), style = RateTextStyle)
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(Spacing.s)) {
                    CoShellStatusChip(stringResource(R.string.rates_origin_manual), StatusTone.Manual)
                    Text(
                        stringResource(R.string.rates_since_date, formatRateDate(rate.date)),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            CoShellIconButton(onClick = onDelete) {
                Icon(painterResource(DesignR.drawable.ic_delete), stringResource(R.string.action_delete))
            }
        }
    }
}
