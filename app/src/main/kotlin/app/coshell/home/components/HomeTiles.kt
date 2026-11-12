package app.coshell.home.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp
import app.coshell.R
import app.coshell.core.designsystem.amount.AmountText
import app.coshell.core.designsystem.amount.splitAmount
import app.coshell.core.designsystem.components.CoShellTile
import app.coshell.core.designsystem.theme.AmountTitleStyle
import app.coshell.core.designsystem.theme.LocalStatusPalette
import app.coshell.core.designsystem.tokens.Spacing
import app.coshell.home.HomeUiState
import app.coshell.ui.format.formatDayMonth
import app.coshell.ui.format.formatMonth

/**
 * The two tiles of «Обзор» (owner 2026-11-10: «2 плитки, как на 17А»): this month's expenses and, next to it,
 * operations without a category — or «Внимание» while a rate is missing or old (board `HomeAttention`).
 * Both tiles share the taller one's height.
 */
@Composable
internal fun HomeTiles(
    state: HomeUiState,
    onTransactions: () -> Unit,
    onRates: () -> Unit,
    onUncategorized: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(modifier.fillMaxWidth().height(IntrinsicSize.Min), horizontalArrangement = Arrangement.spacedBy(Spacing.m)) {
        val tile = Modifier.weight(1f).fillMaxHeight()
        SpendingTile(state.spending, onTransactions, tile)
        if (state.attention.isRatesIssue) {
            RatesTile(state.attention, onRates, tile)
        } else {
            UncategorizedTile(state.attention.uncategorized, onUncategorized, tile)
        }
    }
}

/** «Траты за ноябрь»: the main-currency number, under it its code and the currencies left out («USD · без VES»). */
@Composable
private fun SpendingTile(spending: HomeUiState.MonthSpending?, onClick: () -> Unit, modifier: Modifier) {
    val title = spending?.let { stringResource(R.string.home_spending_title, formatMonth(it.month)) }
        ?: stringResource(R.string.home_spending_tile)
    CoShellTile(title = title, onClick = onClick, modifier = modifier) {
        val main = spending?.totals?.firstOrNull() ?: return@CoShellTile
        val (number, code) = splitAmount(main)
        AmountText(number, AmountTitleStyle, minFontSize = 14.sp)
        val without = spending.missing.takeIf { it.isNotEmpty() }?.let { stringResource(R.string.home_spending_without, it.joinToString(", ")) }
        TileCaption(listOfNotNull(code, without).joinToString(" · "))
    }
}

/** Operations of this month without a category; a tap opens them on «Операции». */
@Composable
private fun UncategorizedTile(count: Int, onClick: () -> Unit, modifier: Modifier) {
    CoShellTile(
        title = stringResource(R.string.home_uncategorized_title),
        onClick = if (count > 0) onClick else null,
        modifier = modifier,
    ) {
        Text(count.toString(), style = AmountTitleStyle)
        TileCaption(pluralStringResource(R.plurals.home_uncategorized_operations, count))
    }
}

/** Missing or old rates, in the warning colour; a tap opens «Курсы». */
@Composable
private fun RatesTile(attention: HomeUiState.Attention, onClick: () -> Unit, modifier: Modifier) {
    val warning = LocalStatusPalette.current.warning
    CoShellTile(
        title = stringResource(R.string.home_attention_title),
        onClick = onClick,
        tint = warning,
        modifier = modifier,
    ) {
        Text(attention.count.toString(), style = AmountTitleStyle, color = warning)
        val lines = buildList {
            if (attention.missingRates.isNotEmpty()) {
                add(stringResource(R.string.home_attention_missing_rates, attention.missingRates.joinToString(", ")))
            }
            attention.staleRatesSince?.let { add(stringResource(R.string.home_attention_stale_rates, formatDayMonth(it))) }
            if (attention.uncategorized > 0) {
                add(pluralStringResource(R.plurals.home_attention_uncategorized, attention.uncategorized, attention.uncategorized))
            }
        }
        TileCaption(lines.joinToString(" · "), color = MaterialTheme.colorScheme.onSurface)
    }
}

@Composable
private fun TileCaption(text: String, color: Color = MaterialTheme.colorScheme.onSurfaceVariant) = Text(
    text,
    style = MaterialTheme.typography.bodySmall,
    color = color,
    maxLines = 3,
    overflow = TextOverflow.Ellipsis,
)
