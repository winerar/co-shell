package app.coshell.home.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.coshell.R
import app.coshell.core.designsystem.amount.AmountText
import app.coshell.core.designsystem.theme.AmountTextStyle
import app.coshell.core.designsystem.theme.AmountTitleStyle
import app.coshell.core.designsystem.theme.LocalAmountPalette
import app.coshell.home.HomeUiState
import app.coshell.ui.format.formatMonth

/** "Overview": this month's expenses in the main report currency (large) and the other ones. */
@Composable
internal fun SpendingCard(spending: HomeUiState.MonthSpending) {
    val palette = LocalAmountPalette.current
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(20.dp)) {
            Text(
                stringResource(R.string.home_spending_title, formatMonth(spending.month)),
                style = MaterialTheme.typography.labelLarge,
            )
            spending.totals.forEachIndexed { index, total ->
                val style = if (index == 0) AmountTitleStyle else AmountTextStyle
                AmountText(
                    text = total,
                    style = style,
                    color = if (index == 0) palette.expense else MaterialTheme.colorScheme.onSurface,
                    minFontSize = 14.sp,
                )
            }
            if (spending.converted) SpendingCaption(stringResource(R.string.home_spending_rate_hint))
            if (spending.missing.isNotEmpty()) {
                SpendingCaption(stringResource(R.string.home_total_missing, spending.missing.joinToString(", ")))
            }
        }
    }
}

@Composable
private fun SpendingCaption(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(top = 4.dp),
    )
}
