package app.coshell.transactions.list.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.state.ToggleableState
import app.coshell.R
import app.coshell.core.designsystem.components.CoShellCheckMark
import app.coshell.core.designsystem.components.CoShellListRow
import app.coshell.core.designsystem.components.CoShellRowGroup
import app.coshell.core.designsystem.components.CoShellSearchField
import app.coshell.core.designsystem.components.CoShellSectionLabel
import app.coshell.core.designsystem.components.SectionTone
import app.coshell.core.designsystem.components.CoShellTagDot
import app.coshell.core.designsystem.tokens.Spacing
import app.coshell.core.domain.tags.TagRanking
import app.coshell.core.model.Tag
import app.coshell.transactions.list.FilterChoices
import app.coshell.transactions.list.ListFilter

/**
 * «Теги» (#294): search by name, then every used tag by name with how many operations have it and a check mark;
 * several chosen — operations with any of them. None — all operations.
 */
@Composable
internal fun TagsSheet(
    filter: ListFilter,
    choices: FilterChoices,
    count: Int?,
    onFilter: ((ListFilter) -> ListFilter) -> Unit,
    onClosed: () -> Unit,
) {
    FilterSheet(
        title = stringResource(R.string.filter_tags_title),
        count = count,
        onReset = { onFilter { it.copy(tagIds = emptySet()) } }.takeIf { filter.tagIds.isNotEmpty() },
        onClosed = onClosed,
    ) { TagsChoices(filter, choices, onFilter) }
}

/** Inside of [TagsSheet]; apart for the screenshot test. */
@Composable
internal fun ColumnScope.TagsChoices(filter: ListFilter, choices: FilterChoices, onFilter: ((ListFilter) -> ListFilter) -> Unit) {
    var query by rememberSaveable { mutableStateOf("") }
    CoShellSearchField(
        value = query,
        onValueChange = { query = it },
        placeholder = stringResource(R.string.filter_tags_search),
        clearLabel = stringResource(R.string.search_clear),
        modifier = Modifier.fillMaxWidth(),
    )
    val found = remember(choices.tags, query) { choices.search(query) }
    Column(verticalArrangement = Arrangement.spacedBy(Spacing.s)) {
        val chosen = choices.tags.count { it.id in filter.tagIds }
        CoShellSectionLabel(
            if (chosen > 0) stringResource(R.string.filter_tags_some, chosen, choices.tags.size)
            else stringResource(R.string.filter_tags_all, choices.tags.size),
            tone = SectionTone.Muted,
        )
        if (found.isEmpty()) {
            Text(
                stringResource(R.string.filter_tags_nothing),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = Spacing.xs),
            )
        } else {
            CoShellRowGroup {
                found.forEach { tag ->
                    row {
                        CoShellListRow(
                            title = tag.name,
                            subtitle = pluralStringResource(R.plurals.filter_total_count, tag.count, tag.count),
                            leading = { CoShellTagDot(tag.color, Modifier.padding(horizontal = Spacing.xs)) },
                            trailing = {
                                CoShellCheckMark(
                                    if (tag.id in filter.tagIds) ToggleableState.On else ToggleableState.Off,
                                    Modifier.padding(end = Spacing.xs),
                                )
                            },
                            onClick = {
                                onFilter { it.copy(tagIds = if (tag.id in it.tagIds) it.tagIds - tag.id else it.tagIds + tag.id) }
                            },
                            dense = true,
                        )
                    }
                }
            }
        }
    }
}

/** Tags whose name holds [query] — names starting with it first ([TagRanking.search]). */
private fun FilterChoices.search(query: String): List<FilterChoices.TagChoice> {
    if (query.isBlank()) return tags
    val byId = tags.associateBy { it.id }
    return TagRanking.search(tags.map { Tag(it.id, it.name) }, query).mapNotNull { byId[it.id] }
}

