package app.coshell.reports.capital

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import app.coshell.R
import app.coshell.accounts.list.components.AccountGroup
import app.coshell.accounts.list.components.AccountRow
import app.coshell.core.designsystem.charts.CoShellLine
import app.coshell.core.designsystem.charts.LinePoint
import app.coshell.core.designsystem.components.CoShellCard
import app.coshell.core.designsystem.components.CoShellFilterChip
import app.coshell.core.designsystem.components.CoShellHint
import app.coshell.core.designsystem.components.CoShellSearchField
import app.coshell.core.designsystem.theme.LocalAmountPalette
import app.coshell.core.designsystem.theme.LocalStatusPalette
import app.coshell.core.designsystem.theme.RateTextStyle
import app.coshell.core.designsystem.tokens.Spacing
import app.coshell.ui.format.formatDayMonth
import app.coshell.ui.format.formatMonth
import app.coshell.ui.format.formatMonthShort
import app.coshell.ui.format.formatMonthYear
import app.coshell.ui.top.ScreenTopBar
import kotlinx.datetime.YearMonth

/** Everything «Капитал · октябрь» can ask for; defaults make previews short. */
data class CapitalMonthActions(
    val onBack: () -> Unit = {},
    val onMonth: (YearMonth) -> Unit = {},
    val onQuery: (String) -> Unit = {},
    val onOrder: (CapitalOrder) -> Unit = {},
    val onToggleGroup: (String) -> Unit = {},
    val onAccount: (String) -> Unit = {},
)

/**
 * «Капитал · октябрь» (#373, board `Rp189NwDrill`): the year line with the month end picked (a tap — another month),
 * capital then and its change, and the accounts that made it up — search, «Группами · По сумме · По изменению»
 * for a long list (24 accounts and more). A tap on an account opens it.
 */
@Composable
fun CapitalMonthScreen(state: CapitalMonthUiState, actions: CapitalMonthActions) {
    val month = state.month
    val title = if (month == null) stringResource(R.string.reports_capital_title)
    else stringResource(R.string.capital_month_title, formatMonth(month.firstDay))
    Scaffold(topBar = { ScreenTopBar(title, onBack = actions.onBack) }) { padding ->
        LazyColumn(
            Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(start = Spacing.l, end = Spacing.l, bottom = Spacing.xl),
            verticalArrangement = Arrangement.spacedBy(Spacing.m),
        ) {
            if (state.points.isEmpty()) return@LazyColumn
            item(key = "head") { Head(state, onMonth = actions.onMonth) }
            item(key = "search") {
                CoShellSearchField(
                    value = state.query,
                    onValueChange = actions.onQuery,
                    placeholder = stringResource(R.string.capital_month_search, state.count),
                    clearLabel = stringResource(R.string.search_clear),
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            item(key = "order") { OrderChips(state.order, actions.onOrder) }
            val nothing = state.groups.isEmpty() && state.rows.isEmpty()
            when {
                nothing -> item(key = "nothing") { Nothing() }
                state.order == CapitalOrder.GROUPS -> items(state.groups, key = { it.key }) { group ->
                    AccountGroup(group, onToggle = { actions.onToggleGroup(group.key) }, onAccountClick = actions.onAccount)
                }
                else -> item(key = "rows") {
                    CoShellCard(Modifier.fillMaxWidth()) {
                        state.rows.forEachIndexed { i, row ->
                            if (i > 0) HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, modifier = Modifier.padding(start = Spacing.l))
                            AccountRow(row, actions.onAccount)
                        }
                    }
                }
            }
            if (state.missing.isNotEmpty()) {
                item(key = "missing") { CoShellHint(stringResource(R.string.reports_missing, state.missing.joinToString())) }
            }
        }
    }
}

@Composable
private fun Head(state: CapitalMonthUiState, onMonth: (YearMonth) -> Unit) {
    val muted = MaterialTheme.colorScheme.onSurfaceVariant
    CoShellCard(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(Spacing.l)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    state.asOf?.let { stringResource(R.string.capital_month_on, formatDayMonth(it)) }.orEmpty(),
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.weight(1f).semantics { heading() },
                )
                Text(stringResource(R.string.capital_month_tap), style = MaterialTheme.typography.bodySmall, color = muted)
            }
            Row(Modifier.padding(top = Spacing.xs), verticalAlignment = Alignment.Bottom) {
                Text(
                    state.value,
                    style = RateTextStyle.copy(fontSize = MaterialTheme.typography.headlineSmall.fontSize, fontWeight = FontWeight.Bold),
                    maxLines = 1,
                )
                Text(
                    " ${state.currency}",
                    style = MaterialTheme.typography.titleSmall,
                    color = muted,
                    modifier = Modifier.padding(end = Spacing.s, bottom = Spacing.xxs),
                )
                state.change?.let {
                    Text(
                        stringResource(R.string.capital_month_change, it),
                        style = MaterialTheme.typography.bodySmall,
                        color = if (state.falling) LocalAmountPalette.current.expense else LocalStatusPalette.current.success,
                        maxLines = 1,
                        modifier = Modifier.padding(bottom = Spacing.xxs),
                    )
                }
            }
            val description = state.points.joinToString(", ") { "${formatMonthYear(it.month)} ${it.value}" }
            CoShellLine(
                points = state.points.map { LinePoint(formatMonthShort(it.month).take(1), it.minor) },
                description = stringResource(R.string.reports_capital_description, description),
                selected = state.selected,
                onSelect = { i -> state.points.getOrNull(i)?.let { onMonth(it.month) } },
                modifier = Modifier.padding(top = Spacing.m),
            )
        }
    }
}

@Composable
private fun OrderChips(order: CapitalOrder, onOrder: (CapitalOrder) -> Unit) {
    val labels = mapOf(
        CapitalOrder.GROUPS to R.string.reports_order_groups,
        CapitalOrder.AMOUNT to R.string.reports_order_amount,
        CapitalOrder.CHANGE to R.string.capital_month_order_change,
    )
    // Three chips may not fit 320 dp in every language: the row scrolls.
    Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(Spacing.s)) {
        labels.forEach { (value, label) ->
            CoShellFilterChip(selected = order == value, onClick = { onOrder(value) }, label = { Text(stringResource(label)) })
        }
    }
}

@Composable
private fun Nothing() {
    CoShellCard(Modifier.fillMaxWidth()) {
        Text(
            stringResource(R.string.reports_all_nothing),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(Spacing.l),
        )
    }
}
