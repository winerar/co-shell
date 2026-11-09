package app.coshell.rates.list.components

import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import app.coshell.R
import app.coshell.core.designsystem.components.CoShellListRow
import app.coshell.core.designsystem.components.CoShellIconButton
import app.coshell.core.designsystem.theme.RateTextStyle
import app.coshell.rates.list.ManualRateItem
import app.coshell.rates.list.formatRateDate
import app.coshell.core.designsystem.R as DesignR

/** A manual rate: "1 USD = 1 450 ARS", "с 5 нояб. 2026 г.", tap edits, the bin deletes. */
@Composable
internal fun ManualRateRow(item: ManualRateItem, onClick: () -> Unit, onDelete: () -> Unit) {
    val rate = item.rate
    CoShellListRow(
        title = { Text(stringResource(R.string.rates_line, rate.from.value, item.rateText, rate.to.value), style = RateTextStyle) },
        supporting = { Text(stringResource(R.string.rates_manual_since, formatRateDate(rate.date))) },
        trailing = {
            CoShellIconButton(onClick = onDelete) {
                Icon(painterResource(DesignR.drawable.ic_delete), stringResource(R.string.action_delete))
            }
        },
        onClick = onClick,
    )
}
