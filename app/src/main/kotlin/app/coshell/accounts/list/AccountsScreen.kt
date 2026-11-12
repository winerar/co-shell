package app.coshell.accounts.list

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import app.coshell.R
import app.coshell.accounts.list.components.AccountGroup
import app.coshell.core.designsystem.amount.AmountText
import app.coshell.core.designsystem.components.CoShellIconButton
import app.coshell.core.designsystem.components.CoShellSegmented
import app.coshell.core.designsystem.components.SegmentSwipe
import app.coshell.core.designsystem.components.SegmentTone
import app.coshell.core.designsystem.components.rememberSegmentSwipe
import app.coshell.core.designsystem.components.segmentSwipe
import app.coshell.core.designsystem.components.CoShellSearchField
import app.coshell.core.designsystem.motion.CoShellExpandable
import app.coshell.core.designsystem.motion.listItemMotion
import app.coshell.core.designsystem.theme.AmountTitleStyle
import app.coshell.core.designsystem.tokens.Sizes
import app.coshell.core.designsystem.tokens.Spacing
import app.coshell.ui.top.TabTopBar
import app.coshell.ui.top.rememberTabScrollBehavior
import app.coshell.ui.top.tabScroll
import app.coshell.core.designsystem.R as DesignR

/** What the accounts tab can ask for; defaults make previews short. */
data class AccountsActions(
    val onAccountClick: (String) -> Unit = {},
    val onAddAccount: () -> Unit = {},
    val onToggleArchived: () -> Unit = {},
    val onGrouping: (AccountGrouping) -> Unit = {},
    val onToggleGroup: (String) -> Unit = {},
    val onQuery: (String) -> Unit = {},
    val onToggleSearch: () -> Unit = {},
)

/**
 * «Счета»: the total in the main currency, a grouping switch (type / currency / whose), foldable groups of
 * compact rows with their totals, archive at the bottom; search and «+» in the top bar.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountsScreen(state: AccountsUiState, actions: AccountsActions) {
    val scroll = rememberTabScrollBehavior()
    val groupings = groupingOptions(state)
    val swipe = rememberSegmentSwipe()
    val grouping = Grouping(
        options = groupings,
        selected = groupings.indexOfFirst { it.first == state.grouping },
        onSelect = { actions.onGrouping(groupings[it].first) },
        swipe = swipe,
        visible = state.active.size > 1,
    )
    Scaffold(
        topBar = {
            TabTopBar(stringResource(R.string.accounts_title), scroll) {
                CoShellIconButton(onClick = actions.onToggleSearch) {
                    Icon(painterResource(DesignR.drawable.ic_search), stringResource(R.string.accounts_search))
                }
                CoShellIconButton(onClick = actions.onAddAccount) {
                    Icon(painterResource(DesignR.drawable.ic_add), stringResource(R.string.accounts_add))
                }
            }
        },
        modifier = Modifier.tabScroll(scroll),
    ) { padding ->
        LazyColumn(
            // The groupings swipe like tabs (#173); from the first or last one the swipe goes to the next tab.
            modifier = Modifier.fillMaxSize().padding(padding)
                .segmentSwipe(swipe, grouping.selected, groupings.size, grouping.onSelect, enabled = grouping.visible),
            contentPadding = PaddingValues(start = Spacing.l, end = Spacing.l, top = Spacing.s, bottom = Sizes.fabClearance),
            verticalArrangement = Arrangement.spacedBy(Spacing.s),
        ) {
            if (state.isLoading) return@LazyColumn
            if (state.active.isEmpty() && state.archived.isEmpty()) {
                item {
                    Text(
                        text = stringResource(R.string.accounts_empty),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                return@LazyColumn
            }
            item(key = "header") { Header(state, actions, grouping) }
            items(state.groups, key = { it.key }) { group ->
                AccountGroup(group, { actions.onToggleGroup(group.key) }, actions.onAccountClick, listItemMotion())
            }
            if (state.archived.isEmpty()) return@LazyColumn
            // The archive folds like the other groups (board «Счета»: «Архив · 2»), muted and without a total.
            item(key = "archived") {
                val archive = AccountsUiState.Group(
                    key = "archived",
                    titleRes = R.string.accounts_archive,
                    rows = state.archived,
                    total = null,
                    expanded = state.showArchived,
                )
                AccountGroup(archive, actions.onToggleArchived, actions.onAccountClick, listItemMotion(), muted = true)
            }
        }
    }
}

@Composable
private fun Header(state: AccountsUiState, actions: AccountsActions, grouping: Grouping) {
    Column(verticalArrangement = Arrangement.spacedBy(Spacing.m)) {
        CoShellExpandable(state.searchOpen) {
            CoShellSearchField(
                value = state.query,
                onValueChange = actions.onQuery,
                placeholder = stringResource(R.string.accounts_search),
                clearLabel = stringResource(R.string.search_clear),
                modifier = Modifier.fillMaxWidth(),
            )
        }
        state.total?.let { total ->
            Column {
                Text(stringResource(R.string.accounts_total), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                AmountText(total, style = AmountTitleStyle)
            }
        }
        if (grouping.visible) {
            CoShellSegmented(
                labels = grouping.options.map { stringResource(it.second) },
                selected = grouping.selected,
                onSelect = grouping.onSelect,
                tone = SegmentTone.View,
                swipe = grouping.swipe,
            )
        }
    }
}

/** The grouping switch, shared by the header (the pill) and the list (the swipe). */
private class Grouping(
    val options: List<Pair<AccountGrouping, Int>>,
    val selected: Int,
    val onSelect: (Int) -> Unit,
    val swipe: SegmentSwipe,
    val visible: Boolean,
)

private fun groupingOptions(state: AccountsUiState): List<Pair<AccountGrouping, Int>> = buildList {
    add(AccountGrouping.TYPE to R.string.accounts_grouping_type)
    add(AccountGrouping.CURRENCY to R.string.accounts_grouping_currency)
    if (state.canGroupByOwner) add(AccountGrouping.OWNER to R.string.accounts_grouping_owner)
}
