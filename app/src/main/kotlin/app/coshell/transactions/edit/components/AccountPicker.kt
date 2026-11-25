package app.coshell.transactions.edit.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import app.coshell.R
import app.coshell.accounts.AccountGroupTitle
import app.coshell.accounts.AccountLine
import app.coshell.accounts.groupByAccountType
import app.coshell.accounts.groupLabelRes
import app.coshell.accounts.labelRes
import app.coshell.core.designsystem.components.CoShellCard
import app.coshell.core.designsystem.components.CoShellDivider
import app.coshell.core.designsystem.components.CoShellSearchField
import app.coshell.core.designsystem.tokens.Sizes
import app.coshell.core.designsystem.tokens.Spacing
import app.coshell.transactions.edit.TransactionEditUiState.AccountOption

private const val SEARCH_FROM = 7
private const val RECENT_MIN = 2

/**
 * The full account list behind «Все ›» of the entry sheet: looks exactly like «Счета» (owner 2026-11-20) — groups by type
 * with a title and count, rows ([AccountLine]: badge, name, type, balance) in one card per group; the recent accounts
 * first, a search field at the very bottom (under the thumb) once there are many. A tap picks the account.
 */
@Composable
internal fun AccountPicker(
    title: String,
    accounts: List<AccountOption>,
    recentIds: List<String>,
    selectedId: String?,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier,
    excludeId: String? = null,
) {
    var query by rememberSaveable { mutableStateOf("") }
    val choices = accounts.filter { it.id != excludeId }
    val found = if (query.isBlank()) choices else choices.filter { it.name.contains(query.trim(), ignoreCase = true) }
    Column(modifier, verticalArrangement = Arrangement.spacedBy(Spacing.m)) {
        Text(title, style = MaterialTheme.typography.titleLarge, modifier = Modifier.fillMaxWidth().padding(horizontal = Spacing.l))
        Column(
            Modifier.weight(1f, fill = false).verticalScroll(rememberScrollState()).padding(horizontal = Spacing.l),
            verticalArrangement = Arrangement.spacedBy(Spacing.s),
        ) {
            val recent = recentIds.mapNotNull { id -> choices.firstOrNull { it.id == id } }
            if (query.isBlank() && recent.size >= RECENT_MIN && choices.size > RECENT_MIN + 2) {
                Group(stringResource(R.string.picker_recent), recent, selectedId, onSelect)
            }
            groupByAccountType(found) { it.type }.forEach { (type, items) ->
                Group(stringResource(type.groupLabelRes), items, selectedId, onSelect)
            }
            if (found.isEmpty()) {
                Text(
                    stringResource(R.string.picker_nothing),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        if (choices.size >= SEARCH_FROM) {
            CoShellSearchField(
                value = query,
                onValueChange = { query = it },
                placeholder = stringResource(R.string.picker_search),
                clearLabel = stringResource(R.string.search_clear),
                modifier = Modifier.fillMaxWidth().padding(horizontal = Spacing.l).padding(bottom = Spacing.m),
            )
        }
    }
}

/** A group as on «Счета» (`AccountGroup`): the title with the count, the rows in one card. */
@Composable
private fun Group(title: String, items: List<AccountOption>, selectedId: String?, onSelect: (String) -> Unit) {
    Column {
        AccountGroupTitle(
            title,
            items.size,
            Modifier.fillMaxWidth().heightIn(min = Sizes.rowMinHeight - Spacing.s).padding(horizontal = Spacing.xs),
        )
        CoShellCard(Modifier.fillMaxWidth()) {
            items.forEachIndexed { index, account ->
                if (index > 0) CoShellDivider(Modifier.padding(start = Spacing.l))
                AccountLine(
                    name = account.name,
                    color = account.color,
                    supporting = stringResource(account.type.labelRes),
                    balance = account.balance,
                    onClick = { onSelect(account.id) },
                    isNegative = account.isNegative,
                    selected = account.id == selectedId,
                )
            }
        }
    }
}
