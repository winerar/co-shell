package app.coshell.transactions.list.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.state.ToggleableState
import app.coshell.R
import app.coshell.core.designsystem.category.CategoryBadge
import app.coshell.core.designsystem.components.CoShellCheckMark
import app.coshell.core.designsystem.components.CoShellExpandIcon
import app.coshell.core.designsystem.components.CoShellFilterChip
import app.coshell.core.designsystem.components.CoShellIconButton
import app.coshell.core.designsystem.components.CoShellListRow
import app.coshell.core.designsystem.components.CoShellRowGroup
import app.coshell.core.designsystem.components.CoShellSearchField
import app.coshell.core.designsystem.components.CoShellSectionLabel
import app.coshell.core.designsystem.components.SectionTone
import app.coshell.core.designsystem.tokens.Sizes
import app.coshell.core.designsystem.tokens.Spacing
import app.coshell.transactions.list.FilterChoices
import app.coshell.transactions.list.FilterChoices.CategoryChoice
import app.coshell.transactions.list.ListFilter
import app.coshell.transactions.list.TransactionsUiState.Kind

/**
 * «Категории» (board `Fb141ACats`): search among all categories, «Все» and «Без категории», then folders with
 * check marks — a folder chooses all its categories («3 из 5» when only some), open it to pick single ones.
 */
@Composable
internal fun CategoriesSheet(
    filter: ListFilter,
    choices: FilterChoices,
    count: Int?,
    onFilter: ((ListFilter) -> ListFilter) -> Unit,
    onClosed: () -> Unit,
) {
    FilterSheet(
        title = stringResource(R.string.filter_categories_title),
        count = count,
        onReset = { onFilter { it.copy(categoryIds = emptySet(), uncategorized = false) } }.takeIf { filter.categoryIds.isNotEmpty() || filter.uncategorized },
        onClosed = onClosed,
    ) { CategoriesChoices(filter, choices, onFilter) }
}

/** Inside of [CategoriesSheet]; apart for the screenshot test. */
@Composable
internal fun ColumnScope.CategoriesChoices(filter: ListFilter, choices: FilterChoices, onFilter: ((ListFilter) -> ListFilter) -> Unit) {
    var query by rememberSaveable { mutableStateOf("") }
    // Folders with only some categories chosen start open, so it is clear which ones.
    var open by rememberSaveable {
        mutableStateOf(
            (choices.expenseCategories + choices.incomeCategories)
                .filter { it.isFolder && choices.chosenIn(it, filter.categoryIds) in 1 until it.children.size }
                .map { it.id }.toSet(),
        )
    }
    CoShellSearchField(
        value = query,
        onValueChange = { query = it },
        placeholder = stringResource(R.string.filter_categories_search, choices.categoryCount),
        clearLabel = stringResource(R.string.search_clear),
        modifier = Modifier.fillMaxWidth(),
    )
    Row(horizontalArrangement = Arrangement.spacedBy(Spacing.s)) {
        CoShellFilterChip(
            selected = filter.categoryIds.isEmpty() && !filter.uncategorized,
            onClick = { onFilter { it.copy(categoryIds = emptySet(), uncategorized = false) } },
            label = { Text(stringResource(R.string.filter_who_all)) },
        )
        CoShellFilterChip(
            selected = filter.uncategorized,
            onClick = { onFilter { it.copy(uncategorized = !it.uncategorized) } },
            label = { Text(stringResource(R.string.filter_uncategorized)) },
        )
    }
    val toggle: (CategoryChoice) -> Unit = { c -> onFilter { it.copy(categoryIds = choices.toggle(it.categoryIds, c)) } }
    val sections = buildList {
        if (filter.kind != Kind.INCOME && filter.kind != Kind.TRANSFER) add(R.string.filter_categories_expenses to choices.expenseCategories)
        if (filter.kind != Kind.EXPENSE && filter.kind != Kind.TRANSFER) add(R.string.filter_categories_incomes to choices.incomeCategories)
    }
    val needle = query.trim().lowercase()
    sections.forEach { (title, groups) ->
        if (groups.isEmpty()) return@forEach
        Column(verticalArrangement = Arrangement.spacedBy(Spacing.s)) {
            if (sections.size > 1) CoShellSectionLabel(stringResource(title), tone = SectionTone.Muted)
            if (needle.isNotEmpty()) {
                // Searching: a flat list of what matches, folders and categories alike.
                val found = groups.flatMap { listOf(it) + it.children }.filter { needle in it.name.lowercase() }
                if (found.isNotEmpty()) {
                    CoShellRowGroup {
                        found.forEach { c -> row { CategoryCheckRow(c, filter.categoryIds, choices, onToggle = { toggle(c) }) } }
                    }
                }
            } else {
                CoShellRowGroup {
                    groups.forEach { group ->
                        row {
                            CategoryCheckRow(
                                group, filter.categoryIds, choices,
                                onToggle = { toggle(group) },
                                expanded = group.id in open,
                                onExpand = { open = if (group.id in open) open - group.id else open + group.id },
                            )
                        }
                        if (group.id in open) group.children.forEach { c ->
                            row { CategoryCheckRow(c, filter.categoryIds, choices, onToggle = { toggle(c) }, indent = true) }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CategoryCheckRow(
    choice: CategoryChoice,
    chosen: Set<String>,
    choices: FilterChoices,
    onToggle: () -> Unit,
    indent: Boolean = false,
    expanded: Boolean = false,
    onExpand: (() -> Unit)? = null,
) {
    val picked = if (choice.isFolder) choices.chosenIn(choice, chosen) else 0
    val state = when {
        !choice.isFolder -> if (choice.id in chosen) ToggleableState.On else ToggleableState.Off
        picked == choice.children.size -> ToggleableState.On
        picked > 0 -> ToggleableState.Indeterminate
        else -> ToggleableState.Off
    }
    CoShellListRow(
        title = choice.name,
        subtitle = when {
            !choice.isFolder -> null
            picked > 0 -> stringResource(R.string.filter_categories_some, picked, choice.children.size)
            else -> pluralStringResource(R.plurals.categories_in_folder, choice.children.size, choice.children.size)
        },
        leading = {
            if (indent) CategoryBadge(choice.icon, choice.color, Modifier.padding(start = Spacing.xl), size = Sizes.badgeSmall)
            else CategoryBadge(choice.icon, choice.color)
        },
        trailing = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (onExpand != null && choice.isFolder) {
                    CoShellIconButton(onClick = onExpand) {
                        CoShellExpandIcon(
                            expanded = expanded,
                            contentDescription = stringResource(if (expanded) R.string.categories_collapse else R.string.categories_expand),
                        )
                    }
                }
                CoShellCheckMark(state, Modifier.padding(end = Spacing.xs))
            }
        },
        onClick = onToggle,
        dense = true,
    )
}
