package app.coshell.transactions.list.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import app.coshell.R
import app.coshell.core.designsystem.components.CoShellTextField
import app.coshell.core.designsystem.tokens.Spacing
import app.coshell.transactions.list.ListFilter
import app.coshell.transactions.list.TransactionsUiState
import app.coshell.transactions.list.TransactionsUiState.Kind

/** Search field and filter chips (type, account, period) at the top of «Операции». */
@Composable
internal fun FilterBar(
    filter: ListFilter,
    accounts: List<TransactionsUiState.AccountChoice>,
    onFilter: ((ListFilter) -> ListFilter) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier.padding(horizontal = Spacing.l), verticalArrangement = Arrangement.spacedBy(Spacing.s)) {
        CoShellTextField(
            value = filter.query,
            onValueChange = { q -> onFilter { it.copy(query = q) } },
            label = stringResource(R.string.transactions_search),
            modifier = Modifier.fillMaxWidth(),
        )
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
            )
            FilterMenuChip(
                label = accounts.firstOrNull { it.id == filter.accountId }?.name ?: stringResource(R.string.filter_account),
                selected = filter.accountId != null,
                options = listOf<Pair<String, () -> Unit>>(stringResource(R.string.filter_all_accounts) to { onFilter { it.copy(accountId = null) } }) +
                    accounts.map { a -> a.name to { onFilter { it.copy(accountId = a.id) } } },
            )
            val periods = listOf(
                ListFilter.Period.ALL to stringResource(R.string.filter_period_all),
                ListFilter.Period.THIS_MONTH to stringResource(R.string.filter_period_this),
                ListFilter.Period.LAST_MONTH to stringResource(R.string.filter_period_last),
            )
            FilterMenuChip(
                label = if (filter.period == ListFilter.Period.ALL) stringResource(R.string.filter_period) else periods.first { it.first == filter.period }.second,
                selected = filter.period != ListFilter.Period.ALL,
                options = periods.map { (period, text) -> text to { onFilter { it.copy(period = period) } } },
            )
        }
    }
}
