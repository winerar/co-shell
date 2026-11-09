package app.coshell.home.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import app.coshell.core.designsystem.components.CoShellMiniBars
import app.coshell.core.designsystem.components.CoShellTile
import app.coshell.core.designsystem.theme.AmountTextStyle
import app.coshell.core.designsystem.theme.AmountTitleStyle
import app.coshell.core.designsystem.theme.LocalAmountPalette
import app.coshell.core.designsystem.theme.LocalStatusPalette
import app.coshell.core.designsystem.theme.accentInk
import app.coshell.core.designsystem.tokens.Spacing
import app.coshell.home.HomeUiState
import app.coshell.ui.format.formatDayMonth
import app.coshell.core.designsystem.R as DesignR

/**
 * The bento grid of «Обзор» (design-refactor Step 5, owner's choice): expenses with daily bars and income
 * in the first row, «Внимание» and accounts in the second. Tiles of a row share the taller one's height.
 */
@Composable
internal fun HomeTiles(
    state: HomeUiState,
    onTransactions: () -> Unit,
    onAccounts: () -> Unit,
    onRates: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(Spacing.m)) {
        TileRow(
            { SpendingTile(state.spending, onTransactions, it) },
            { IncomeTile(state.income, onTransactions, it) },
        )
        TileRow(
            { AttentionTile(state.attention, onClick = if (state.attention.isRatesIssue) onRates else onTransactions, it) },
            { AccountsTile(state.accounts, onAccounts, it) },
        )
    }
}

@Composable
private fun TileRow(start: @Composable (Modifier) -> Unit, end: @Composable (Modifier) -> Unit) {
    Row(Modifier.fillMaxWidth().height(IntrinsicSize.Min), horizontalArrangement = Arrangement.spacedBy(Spacing.m)) {
        start(Modifier.weight(1f).fillMaxHeight())
        end(Modifier.weight(1f).fillMaxHeight())
    }
}

/** This month's expenses in the main currency, other report currencies under it, bars per day. */
@Composable
private fun SpendingTile(spending: HomeUiState.MonthSpending?, onClick: () -> Unit, modifier: Modifier) {
    val color = LocalAmountPalette.current.expense
    CoShellTile(
        title = stringResource(R.string.home_spending_tile),
        icon = DesignR.drawable.ic_shopping_bag,
        iconColor = color,
        onClick = onClick,
        modifier = modifier,
        footer = spending?.daily?.takeIf { it.isNotEmpty() }?.let { daily ->
            { CoShellMiniBars(daily, color, elapsed = spending.today + 1, highlight = spending.today) }
        },
    ) {
        spending?.let { MainAndOthers(it.totals, color) }
    }
}

/** This month's income: main currency large, the next report currency small. */
@Composable
private fun IncomeTile(income: HomeUiState.MonthTotals?, onClick: () -> Unit, modifier: Modifier) {
    val color = LocalAmountPalette.current.income
    CoShellTile(
        title = stringResource(R.string.home_income_title),
        icon = DesignR.drawable.ic_savings,
        iconColor = color,
        onClick = onClick,
        modifier = modifier,
    ) {
        income?.let { MainAndOthers(it.totals, color) }
    }
}

@Composable
private fun MainAndOthers(totals: List<String>, color: Color) {
    totals.firstOrNull()?.let { AmountText(it, AmountTitleStyle, color = color, minFontSize = 14.sp) }
    totals.drop(1).take(1).forEach {
        AmountText(it, AmountTextStyle, color = MaterialTheme.colorScheme.onSurfaceVariant, minFontSize = 12.sp)
    }
}

/** Missing or old rates and operations without a category; «Всё в порядке» when there is nothing. */
@Composable
private fun AttentionTile(attention: HomeUiState.Attention, onClick: () -> Unit, modifier: Modifier) {
    val ok = attention.count == 0
    val warning = LocalStatusPalette.current.warning
    CoShellTile(
        title = stringResource(R.string.home_attention_title),
        icon = if (ok) DesignR.drawable.ic_check else DesignR.drawable.ic_lightbulb,
        iconColor = if (ok) accentInk() else warning,
        onClick = if (ok) null else onClick,
        modifier = modifier,
    ) {
        if (ok) {
            Text(stringResource(R.string.home_attention_ok), style = MaterialTheme.typography.titleMedium)
            return@CoShellTile
        }
        Text(attention.count.toString(), style = AmountTitleStyle, color = warning)
        if (attention.missingRates.isNotEmpty()) {
            TileLine(stringResource(R.string.home_attention_missing_rates, attention.missingRates.joinToString(", ")))
        }
        attention.staleRatesSince?.let { TileLine(stringResource(R.string.home_attention_stale_rates, formatDayMonth(it))) }
        if (attention.uncategorized > 0) {
            TileLine(pluralStringResource(R.plurals.home_attention_uncategorized, attention.uncategorized, attention.uncategorized))
        }
    }
}

/** How many accounts, their names (the first ones) and how many stay out of the total. */
@Composable
private fun AccountsTile(accounts: List<HomeUiState.AccountRow>, onClick: () -> Unit, modifier: Modifier) {
    CoShellTile(
        title = stringResource(R.string.home_accounts),
        icon = DesignR.drawable.ic_account_balance_wallet,
        onClick = onClick,
        modifier = modifier,
    ) {
        Text(accounts.size.toString(), style = AmountTitleStyle)
        Text(
            accounts.joinToString(", ") { it.name },
            style = MaterialTheme.typography.bodySmall,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
        val excluded = accounts.count { it.excludedFromTotal }
        if (excluded > 0) TileLine(stringResource(R.string.home_accounts_excluded, excluded))
    }
}

@Composable
private fun TileLine(text: String) = Text(
    text,
    style = MaterialTheme.typography.bodySmall,
    color = MaterialTheme.colorScheme.onSurfaceVariant,
    maxLines = 2,
    overflow = TextOverflow.Ellipsis,
    modifier = Modifier.padding(top = Spacing.xxs),
)
