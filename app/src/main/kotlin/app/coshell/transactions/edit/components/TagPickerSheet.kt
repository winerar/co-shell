package app.coshell.transactions.edit.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.window.Dialog
import app.coshell.R
import app.coshell.core.designsystem.R as DesignR
import app.coshell.core.designsystem.components.CoShellCheckMark
import app.coshell.core.designsystem.components.CoShellListRow
import app.coshell.core.designsystem.components.CoShellRowGroup
import app.coshell.core.designsystem.components.CoShellSearchField
import app.coshell.core.designsystem.components.CoShellSectionLabel
import app.coshell.core.designsystem.components.CoShellSheet
import app.coshell.core.designsystem.components.CoShellSheetDialogProperties
import app.coshell.core.designsystem.components.CoShellSheetLayout
import app.coshell.core.designsystem.components.CoShellTagChip
import app.coshell.core.designsystem.components.CoShellTagDot
import app.coshell.core.designsystem.components.CoShellTextButton
import app.coshell.core.designsystem.components.SectionTone
import app.coshell.core.designsystem.theme.accentInk
import app.coshell.core.designsystem.tokens.Sizes
import app.coshell.core.designsystem.tokens.Spacing
import app.coshell.transactions.edit.RECENT_IN_SHEET
import app.coshell.transactions.edit.TagOption
import app.coshell.transactions.edit.TransactionEditActions
import app.coshell.transactions.edit.TransactionEditUiState
import app.coshell.transactions.edit.searchTags

/**
 * «Теги» over the entry sheet (#293, board `Fb129Pick`): «Найти или создать тег», the chosen tags with «✕»,
 * «Новый тег — название и цвет», «Недавние» and every tag by name with how many operations have it.
 * Choices apply at once; «Готово» only closes.
 */
@Composable
internal fun TagPickerSheet(state: TransactionEditUiState, actions: TransactionEditActions, onClosed: () -> Unit) {
    var newTagName by rememberSaveable { mutableStateOf<String?>(null) }
    Dialog(onDismissRequest = onClosed, properties = CoShellSheetDialogProperties) {
        CoShellSheet(onClosed = onClosed) { close ->
            CoShellSheetLayout(
                title = stringResource(R.string.tags_sheet_title),
                actionLabel = stringResource(R.string.tags_sheet_done),
                onAction = close,
                showAction = false,
                titleAction = { CoShellTextButton(onClick = close) { Text(stringResource(R.string.tags_sheet_done)) } },
            ) { TagPickerContent(state, actions.onTagToggle, onNew = { newTagName = it }) }
        }
    }
    newTagName?.let { name ->
        NewTagDialog(
            initialName = name,
            onCreate = { tagName, color ->
                actions.onTagCreate(tagName, color)
                newTagName = null
            },
            onDismiss = { newTagName = null },
        )
    }
}

/** Inside of [TagPickerSheet]; apart for the screenshot test. [onNew] gets the typed text as the name to start from. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun ColumnScope.TagPickerContent(state: TransactionEditUiState, onToggle: (String) -> Unit, onNew: (String) -> Unit) {
    var query by rememberSaveable { mutableStateOf("") }
    val options = state.tags.options
    val byId = remember(options) { options.associateBy { it.id } }
    CoShellSearchField(
        value = query,
        onValueChange = { query = it },
        placeholder = stringResource(R.string.tags_search),
        clearLabel = stringResource(R.string.search_clear),
        modifier = Modifier.fillMaxWidth(),
    )
    val chosen = state.tagIds.mapNotNull { byId[it] }
    if (chosen.isNotEmpty()) {
        FlowRow(horizontalArrangement = Arrangement.spacedBy(Spacing.s), verticalArrangement = Arrangement.spacedBy(Spacing.s)) {
            chosen.forEach { tag ->
                CoShellTagChip(
                    tag.name, tag.color, selected = true, onClick = { onToggle(tag.id) },
                    removeLabel = stringResource(R.string.tags_remove),
                )
            }
        }
    }
    val found = remember(options, query) { searchTags(options, query) }
    NewTagRow(query, onClick = { onNew(query.trim()) })
    if (options.isEmpty()) {
        Hint(stringResource(R.string.tags_empty))
        return
    }
    val recent = state.tags.recentIds.take(RECENT_IN_SHEET).mapNotNull { byId[it] }
    if (query.isBlank() && recent.isNotEmpty()) {
        TagSection(stringResource(R.string.tags_recent), recent, state.tagIds, onToggle)
    }
    if (found.isEmpty()) {
        Hint(stringResource(R.string.tags_nothing))
    } else {
        TagSection(stringResource(R.string.tags_all_by_name, options.size), found, state.tagIds, onToggle)
    }
}

/** «＋ Новый тег — название и цвет»; with a typed name — «＋ Новый тег «бассейн»». */
@Composable
private fun NewTagRow(query: String, onClick: () -> Unit) {
    val ink = accentInk()
    CoShellListRow(
        title = {
            Text(
                if (query.isBlank()) stringResource(R.string.tags_new) else stringResource(R.string.tags_new_named, query.trim()),
                color = ink,
                style = MaterialTheme.typography.bodyLarge,
            )
        },
        leading = { Icon(painterResource(DesignR.drawable.ic_add), null, Modifier.size(Sizes.tileIcon), tint = ink) },
        onClick = onClick,
        dense = true,
    )
}

@Composable
private fun TagSection(title: String, tags: List<TagOption>, chosen: List<String>, onToggle: (String) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(Spacing.s)) {
        CoShellSectionLabel(title, tone = SectionTone.Muted)
        CoShellRowGroup {
            tags.forEach { tag ->
                row {
                    CoShellListRow(
                        title = tag.name,
                        leading = { CoShellTagDot(tag.color, Modifier.padding(horizontal = Spacing.xs)) },
                        trailing = {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(Spacing.m)) {
                                if (tag.count > 0) {
                                    Text(
                                        tag.count.toString(),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                                CoShellCheckMark(
                                    if (tag.id in chosen) ToggleableState.On else ToggleableState.Off,
                                    Modifier.padding(end = Spacing.xs),
                                )
                            }
                        },
                        onClick = { onToggle(tag.id) },
                        dense = true,
                    )
                }
            }
        }
    }
}

@Composable
private fun Hint(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(horizontal = Spacing.xs),
    )
}
