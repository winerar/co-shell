package app.coshell.transactions.edit.components

import app.coshell.core.designsystem.components.SectionTone
import app.coshell.core.designsystem.components.CoShellSectionLabel
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import app.coshell.R
import app.coshell.accounts.groupByAccountType
import app.coshell.accounts.groupLabelRes
import app.coshell.core.designsystem.category.AccountBadge
import app.coshell.core.designsystem.components.CoShellFilterChip
import app.coshell.core.designsystem.components.CoShellSearchField
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.widthIn
import app.coshell.core.designsystem.tokens.Sizes
import app.coshell.core.designsystem.tokens.Spacing
import app.coshell.transactions.edit.TransactionEditUiState.AccountOption

private const val SEARCH_FROM = 7

/**
 * Step 1 of the entry sheet: «Откуда списали?» — the recent accounts big on top, then every account in a
 * wrapping chips by group, a search field at the very bottom (under the thumb) once there are many.
 * A tap picks the account and the sheet moves on.
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
        Row(Modifier.fillMaxWidth().padding(horizontal = Spacing.l), verticalAlignment = Alignment.Bottom) {
            Text(title, style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f))
            Text(
                pluralStringResource(R.plurals.picker_account_count, choices.size, choices.size),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Column(
            Modifier.weight(1f, fill = false).verticalScroll(rememberScrollState()).padding(horizontal = Spacing.l),
            verticalArrangement = Arrangement.spacedBy(Spacing.s),
        ) {
            val recent = recentIds.mapNotNull { id -> choices.firstOrNull { it.id == id } }
            if (query.isBlank() && recent.size >= RECENT_MIN && choices.size > RECENT_MIN + 2) {
                GroupTitle(stringResource(R.string.picker_recent), accent = true)
                AccountChips(recent, selectedId, onSelect)
            }
            groupByAccountType(found) { it.type }.forEach { (type, items) ->
                GroupTitle(stringResource(type.groupLabelRes).uppercase())
                AccountChips(items, selectedId, onSelect)
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

private const val RECENT_MIN = 2

@Composable
private fun GroupTitle(text: String, accent: Boolean = false) =
    CoShellSectionLabel(text, Modifier.padding(top = Spacing.s), tone = if (accent) SectionTone.Accent else SectionTone.Muted)

/** Accounts as choice chips (poll #367 А): the account's badge and its name; the chosen one is neon with a frame. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AccountChips(accounts: List<AccountOption>, selectedId: String?, onSelect: (String) -> Unit) {
    FlowRow(horizontalArrangement = Arrangement.spacedBy(Spacing.s), verticalArrangement = Arrangement.spacedBy(Spacing.s)) {
        accounts.forEach { account ->
            CoShellFilterChip(
                selected = account.id == selectedId,
                onClick = { onSelect(account.id) },
                label = { Text(account.name, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.widthIn(max = Sizes.tileMinHeight)) },
                leadingIcon = { AccountBadge(account.name, account.color, size = Sizes.chipBadge) },
            )
        }
    }
}
