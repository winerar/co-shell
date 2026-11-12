package app.coshell.rates.list.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
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
 * A manual rate as a violet card with a bar (board `Rates`): «1 USD = 1 450 ARS»,
 * «вручную · с 5 ноября · важнее загруженного»; tap edits, the bin deletes.
 */
@Composable
internal fun ManualRateRow(item: ManualRateItem, onClick: () -> Unit, onDelete: () -> Unit) {
    val rate = item.rate
    val violet = MaterialTheme.colorScheme.secondary
    CoShellCard(
        Modifier.fillMaxWidth().padding(horizontal = Spacing.l, vertical = Spacing.xs),
        tint = violet,
        onClick = onClick,
    ) {
        Row(
            Modifier.height(IntrinsicSize.Min).padding(start = Spacing.m, top = Spacing.m, bottom = Spacing.m),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Spacing.m),
        ) {
            Box(Modifier.width(Spacing.xs).fillMaxHeight().background(violet, RoundedCornerShape(Spacing.xs / 2)))
            Column(Modifier.weight(1f)) {
                Text(stringResource(R.string.rates_line, rate.from.value, item.rateText, rate.to.value), style = RateTextStyle)
                Text(
                    stringResource(R.string.rates_manual_since, formatRateDate(rate.date)),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            CoShellIconButton(onClick = onDelete) {
                Icon(painterResource(DesignR.drawable.ic_delete), stringResource(R.string.action_delete))
            }
        }
    }
}
