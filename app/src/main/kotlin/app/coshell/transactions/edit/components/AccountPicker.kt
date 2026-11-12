package app.coshell.transactions.edit.components

import app.coshell.core.designsystem.components.SectionTone
import app.coshell.core.designsystem.components.CoShellSectionLabel
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import app.coshell.R
import app.coshell.accounts.groupByAccountType
import app.coshell.accounts.groupLabelRes
import app.coshell.core.designsystem.category.AccountBadge
import app.coshell.core.designsystem.category.categoryColor
import app.coshell.core.designsystem.components.CoShellSearchField
import app.coshell.core.designsystem.motion.Tactile
import app.coshell.core.designsystem.motion.selectionPop
import app.coshell.core.designsystem.motion.tactileClickable
import app.coshell.core.designsystem.theme.RateTextStyle
import app.coshell.core.designsystem.theme.accentInk
import app.coshell.core.designsystem.tokens.Motion
import app.coshell.core.designsystem.tokens.Sizes
import app.coshell.core.designsystem.tokens.Spacing
import app.coshell.transactions.edit.TransactionEditUiState.AccountOption

private const val COLUMNS = 3
private const val SEARCH_FROM = 7

/**
 * Step 1 of the entry sheet: «Откуда списали?» — the recent accounts big on top, then every account in a
 * 3-column grid by group, a search field at the very bottom (under the thumb) once there are many.
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
        Text(title, style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(horizontal = Spacing.l))
        Column(
            Modifier.weight(1f, fill = false).verticalScroll(rememberScrollState()).padding(horizontal = Spacing.l),
            verticalArrangement = Arrangement.spacedBy(Spacing.s),
        ) {
            val recent = recentIds.mapNotNull { id -> choices.firstOrNull { it.id == id } }
            if (query.isBlank() && recent.size > 1 && choices.size > COLUMNS + 1) {
                GroupTitle(stringResource(R.string.picker_recent), accent = true)
                Row(horizontalArrangement = Arrangement.spacedBy(Spacing.s)) {
                    recent.forEach { RecentTile(it, it.id == selectedId, Modifier.weight(1f)) { onSelect(it.id) } }
                    repeat(RECENT_SLOTS - recent.size) { Box(Modifier.weight(1f)) }
                }
            }
            groupByAccountType(found) { it.type }.forEach { (type, items) ->
                GroupTitle(stringResource(type.groupLabelRes))
                items.chunked(COLUMNS).forEach { line ->
                    Row(horizontalArrangement = Arrangement.spacedBy(Spacing.s)) {
                        line.forEach { AccountTile(it, it.id == selectedId, Modifier.weight(1f)) { onSelect(it.id) } }
                        repeat(COLUMNS - line.size) { Box(Modifier.weight(1f)) }
                    }
                }
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

private const val RECENT_SLOTS = 4

@Composable
private fun GroupTitle(text: String, accent: Boolean = false) =
    CoShellSectionLabel(text, Modifier.padding(top = Spacing.s), tone = if (accent) SectionTone.Accent else SectionTone.Muted)

@Composable
private fun RecentTile(account: AccountOption, selected: Boolean, modifier: Modifier, onClick: () -> Unit) {
    val ring by animateColorAsState(ringColor(account, selected), Motion.effects(), label = "recentRing")
    val shape = MaterialTheme.shapes.large
    Column(
        modifier = modifier
            .tactileClickable(Tactile.Control, shape, onClick = onClick)
            .selectionPop(selected)
            .border(Sizes.selectionRing, ring, shape)
            .padding(vertical = Spacing.s, horizontal = Spacing.xs),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(Spacing.xs),
    ) {
        AccountBadge(account.name, account.color)
        Text(account.name, style = MaterialTheme.typography.labelMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
private fun AccountTile(account: AccountOption, selected: Boolean, modifier: Modifier, onClick: () -> Unit) {
    val ring by animateColorAsState(ringColor(account, selected), Motion.effects(), label = "tileRing")
    val shape = MaterialTheme.shapes.large
    Column(
        modifier = modifier
            .heightIn(min = Sizes.accountTile)
            .tactileClickable(Tactile.Control, shape, onClick = onClick)
            .selectionPop(selected)
            .background(MaterialTheme.colorScheme.surfaceContainerHigh, shape)
            .border(Sizes.selectionRing, ring, shape)
            .padding(Spacing.m),
        verticalArrangement = Arrangement.spacedBy(Spacing.xs),
    ) {
        AccountBadge(account.name, account.color, size = Sizes.badgeSmall)
        Text(account.name, style = MaterialTheme.typography.labelLarge, maxLines = 1, overflow = TextOverflow.Ellipsis)
        Text(
            account.balance.ifEmpty { account.currency },
            style = RateTextStyle,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun ringColor(account: AccountOption, selected: Boolean): Color = when {
    !selected -> Color.Transparent
    account.color != null -> categoryColor(account.color)
    else -> accentInk()
}
