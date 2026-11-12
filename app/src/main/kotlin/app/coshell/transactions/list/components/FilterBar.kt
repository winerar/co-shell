package app.coshell.transactions.list.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.material3.FilterChipDefaults
import app.coshell.R
import app.coshell.core.designsystem.components.CoShellSearchField
import app.coshell.core.designsystem.tokens.Spacing
import app.coshell.core.domain.search.AmountQuery
import app.coshell.transactions.list.FilterLabels
import app.coshell.transactions.list.ListFilter
import app.coshell.transactions.list.TransactionsUiState
import app.coshell.transactions.list.TransactionsUiState.Kind
import app.coshell.core.designsystem.R as DesignR

/** Which filter sheet is open. */
private enum class OpenSheet { CATEGORIES, PERIOD, ACCOUNTS }

/**
 * Top of «Операции» (#141, variant А): the search field (with a line on how an amount query was understood),
 * the filter chips — type, category, period, account, who — each with its own sheet, and the total of what
 * was found (4А) while any filter is set.
 */
@Composable
internal fun FilterBar(
    state: TransactionsUiState,
    onFilter: ((ListFilter) -> ListFilter) -> Unit,
    modifier: Modifier = Modifier,
) {
    val filter = state.filter
    val choices = state.choices
    var open by rememberSaveable { mutableStateOf<OpenSheet?>(null) }
    val locale = LocalConfiguration.current.locales[0]
    Column(modifier.padding(horizontal = Spacing.l), verticalArrangement = Arrangement.spacedBy(Spacing.s)) {
        CoShellSearchField(
            value = filter.query,
            onValueChange = { q -> onFilter { it.copy(query = q) } },
            placeholder = stringResource(R.string.transactions_search),
            clearLabel = stringResource(R.string.search_clear),
            modifier = Modifier.fillMaxWidth(),
        )
        filter.amount?.let { AmountHint(it) }
        Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(Spacing.s)) {
            val kinds = listOf(
                null to stringResource(R.string.filter_all_types),
                Kind.EXPENSE to stringResource(R.string.filter_expenses),
                Kind.INCOME to stringResource(R.string.filter_incomes),
                Kind.TRANSFER to stringResource(R.string.filter_transfers),
            )
            FilterMenuChip(
                label = kinds.firstOrNull { it.first == filter.kind && filter.kind != null }?.second ?: stringResource(R.string.filter_type),
                selected = filter.kind != null,
                options = kinds.map { (kind, text) -> text to { onFilter { it.copy(kind = kind) } } },
                onClear = { onFilter { it.copy(kind = null) } },
            )
            val category = FilterLabels.categories(filter, choices, stringResource(R.string.filter_uncategorized))
            FilterSheetChip(
                label = category ?: stringResource(R.string.filter_category),
                selected = category != null,
                onClick = { open = OpenSheet.CATEGORIES },
                onClear = { onFilter { it.copy(categoryIds = emptySet(), uncategorized = false) } },
            )
            val period = filter.period
            FilterSheetChip(
                label = if (period != null && state.today != null) FilterLabels.period(period, state.today, locale) else stringResource(R.string.filter_period),
                selected = period != null,
                onClick = { open = OpenSheet.PERIOD },
                onClear = { onFilter { it.copy(period = null) } },
            )
            val who = FilterLabels.who(filter, choices, stringResource(R.string.filter_me))
            if (who != null) {
                FilterSheetChip(who, selected = true, onClick = { open = OpenSheet.ACCOUNTS }, onClear = { onFilter { it.copy(who = null) } })
            }
            val account = FilterLabels.accounts(filter, choices)
            FilterSheetChip(
                label = account ?: stringResource(R.string.filter_account),
                selected = account != null,
                onClick = { open = OpenSheet.ACCOUNTS },
                onClear = { onFilter { it.copy(accountIds = emptySet()) } },
            )
        }
        state.total?.let { FilterTotalCard(it) }
    }

    val count = state.total?.count
    val close = { open = null }
    when (open) {
        OpenSheet.CATEGORIES -> CategoriesSheet(filter, choices, count, onFilter, close)
        OpenSheet.PERIOD -> state.today?.let { PeriodSheet(filter, it, count, onFilter, close) }
        OpenSheet.ACCOUNTS -> AccountsSheet(filter, choices, count, onFilter, close)
        null -> Unit
    }
}

/** «Сумма от 3 000 до 5 000 — в любой валюте, по счёту и по чеку» (board `Fb141AAmount`). */
@Composable
private fun AmountHint(query: AmountQuery) {
    val bounds = FilterLabels.amountBounds(query)
    val text = when (query.kind) {
        AmountQuery.Kind.EXACT -> stringResource(R.string.amount_hint_exact, bounds[0])
        AmountQuery.Kind.RANGE -> stringResource(R.string.amount_hint_range, bounds[0], bounds[1])
        AmountQuery.Kind.MORE -> stringResource(R.string.amount_hint_more, bounds[0])
        AmountQuery.Kind.AT_LEAST -> stringResource(R.string.amount_hint_at_least, bounds[0])
        AmountQuery.Kind.LESS -> stringResource(R.string.amount_hint_less, bounds[0])
        AmountQuery.Kind.AT_MOST -> stringResource(R.string.amount_hint_at_most, bounds[0])
    }
    Row(Modifier.padding(horizontal = Spacing.xs), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(Spacing.xs)) {
        Icon(painterResource(DesignR.drawable.ic_check), contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(FilterChipDefaults.IconSize))
        Text(text, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
