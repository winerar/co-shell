package app.coshell.transactions.list.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.state.ToggleableState
import app.coshell.R
import app.coshell.core.designsystem.category.AccountBadge
import app.coshell.core.designsystem.components.CoShellCheckMark
import app.coshell.core.designsystem.components.CoShellListRow
import app.coshell.core.designsystem.components.CoShellRowGroup
import app.coshell.core.designsystem.components.CoShellSectionLabel
import app.coshell.core.designsystem.components.CoShellSegmented
import app.coshell.core.designsystem.components.SectionTone
import app.coshell.core.designsystem.tokens.Spacing
import app.coshell.transactions.list.FilterChoices
import app.coshell.transactions.list.ListFilter

/**
 * «Счёт и кто внёс» (board `Fb141AAcc`): «Все · Я · Аня» on top (only in a family), then every account with a
 * check mark; none checked — all accounts.
 */
@Composable
internal fun AccountsSheet(
    filter: ListFilter,
    choices: FilterChoices,
    count: Int?,
    onFilter: ((ListFilter) -> ListFilter) -> Unit,
    onClosed: () -> Unit,
) {
    val family = choices.members.isNotEmpty()
    FilterSheet(
        title = stringResource(if (family) R.string.filter_accounts_title else R.string.filter_accounts_title_alone),
        count = count,
        onReset = { onFilter { it.copy(accountIds = emptySet(), who = null) } }.takeIf { filter.accountIds.isNotEmpty() || filter.who != null },
        onClosed = onClosed,
    ) { AccountsChoices(filter, choices, onFilter) }
}

/** Inside of [AccountsSheet]; apart for the screenshot test. */
@Composable
internal fun ColumnScope.AccountsChoices(filter: ListFilter, choices: FilterChoices, onFilter: ((ListFilter) -> ListFilter) -> Unit) {
    val family = choices.members.isNotEmpty()
    if (family) {
        Column(verticalArrangement = Arrangement.spacedBy(Spacing.s)) {
            CoShellSectionLabel(stringResource(R.string.filter_who), tone = SectionTone.Muted)
            val options: List<ListFilter.Who?> = listOf(null, ListFilter.Who.Me) + choices.members.map { ListFilter.Who.Member(it.id) }
            val labels = listOf(stringResource(R.string.filter_who_all), stringResource(R.string.filter_me)) + choices.members.map { it.name }
            CoShellSegmented(
                labels = labels,
                selected = options.indexOf(filter.who).coerceAtLeast(0),
                onSelect = { i -> onFilter { it.copy(who = options[i]) } },
            )
        }
    }
    Column(verticalArrangement = Arrangement.spacedBy(Spacing.s)) {
        val chosen = choices.accounts.count { it.id in filter.accountIds }
        CoShellSectionLabel(
            if (chosen > 0) stringResource(R.string.filter_accounts_some, chosen, choices.accounts.size)
            else stringResource(R.string.filter_all_accounts),
            tone = SectionTone.Muted,
        )
        CoShellRowGroup {
            choices.accounts.forEach { account ->
                row {
                    CoShellListRow(
                        title = account.name,
                        subtitle = account.currency,
                        leading = { AccountBadge(account.name, account.color) },
                        trailing = {
                            CoShellCheckMark(
                                if (account.id in filter.accountIds) ToggleableState.On else ToggleableState.Off,
                                Modifier.padding(end = Spacing.xs),
                            )
                        },
                        onClick = {
                            onFilter {
                                it.copy(accountIds = if (account.id in it.accountIds) it.accountIds - account.id else it.accountIds + account.id)
                            }
                        },
                        dense = true,
                    )
                }
            }
        }
    }
}
