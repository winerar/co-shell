package app.coshell.ui.form

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import app.coshell.R
import app.coshell.core.designsystem.category.CategoryBadge
import app.coshell.core.designsystem.components.CoShellListRow
import app.coshell.core.designsystem.components.CoShellSearchField
import app.coshell.core.designsystem.components.CoShellSectionLabel
import app.coshell.core.designsystem.tokens.Spacing
import app.coshell.ui.sheet.ModalSheet
import app.coshell.core.designsystem.R as DesignR

/**
 * One line of a [ChoiceSheet] (#439). [group] — the caption above a run of lines with the same group (options come
 * ordered by it); [icon] and [color] — the category badge on the left ([hasBadge] — also for a category with neither).
 */
data class ChoiceOption<T>(
    val value: T,
    val label: String,
    val subtitle: String? = null,
    val group: String? = null,
    val icon: String? = null,
    val color: Long? = null,
    val hasBadge: Boolean = icon != null || color != null,
)

/**
 * A sheet to pick one of [options] (#345): rows with a check on the selected one, a search above when there
 * are more than [ChoiceSearch.MIN_OPTIONS]. A tap picks: the sheet slides down, then [onPick] runs (it closes
 * the sheet as [onDismiss] does). [hint] — the subtitle under the title. [extra] turns the search text into one more
 * option (a currency code not in the list); with it the search is always shown.
 */
@Composable
fun <T> ChoiceSheet(
    title: String,
    options: List<ChoiceOption<T>>,
    selected: T?,
    onPick: (T) -> Unit,
    onDismiss: () -> Unit,
    hint: String? = null,
    extra: ((query: String) -> ChoiceOption<T>?)? = null,
    searchPlaceholder: String = stringResource(R.string.choice_search),
    alwaysSearch: Boolean = false,
) {
    var picked by remember { mutableStateOf<Picked<T>?>(null) }
    var query by rememberSaveable { mutableStateOf("") }
    ModalSheet(title = title, subtitle = hint, onClosed = { picked?.let { onPick(it.value) } ?: onDismiss() }) { close ->
        ChoiceRows(
            options = options,
            selected = selected,
            query = query,
            onQueryChange = { query = it },
            showSearch = alwaysSearch || extra != null || ChoiceSearch.shows(options.size),
            searchPlaceholder = searchPlaceholder,
            extra = extra,
        ) {
            picked = Picked(it)
            close()
        }
    }
}

/** Search and rows of a [ChoiceSheet]; apart for the screenshot test. */
@Composable
internal fun <T> ColumnScope.ChoiceRows(
    options: List<ChoiceOption<T>>,
    selected: T?,
    query: String,
    onQueryChange: (String) -> Unit,
    showSearch: Boolean,
    searchPlaceholder: String,
    extra: ((query: String) -> ChoiceOption<T>?)? = null,
    onPick: (T) -> Unit,
) {
    if (showSearch) {
        CoShellSearchField(
            value = query,
            onValueChange = onQueryChange,
            placeholder = searchPlaceholder,
            clearLabel = stringResource(R.string.search_clear),
        )
    }
    val typed = extra?.invoke(query)?.takeIf { e -> options.none { it.value == e.value } }
    val shown = listOfNotNull(typed) + ChoiceSearch.filter(options, query)
    // The sheet's column spaces its children; the rows sit in a column of their own so they stay one under another.
    Column(Modifier.fillMaxWidth()) {
        var lastGroup: String? = null
        shown.forEach { option ->
            if (option.group != null && option.group != lastGroup) CoShellSectionLabel(option.group, Modifier.padding(top = Spacing.s))
            lastGroup = option.group
            CoShellListRow(
                title = { Text(option.label, maxLines = 2, overflow = TextOverflow.Ellipsis) },
                supporting = option.subtitle?.let { { Text(it, maxLines = 1, overflow = TextOverflow.Ellipsis) } },
                leading = if (option.hasBadge) {
                    { CategoryBadge(option.icon, option.color) }
                } else {
                    null
                },
                trailing = if (option.value == selected) {
                    { Icon(painterResource(DesignR.drawable.ic_check), contentDescription = null) }
                } else {
                    null
                },
                onClick = { onPick(option.value) },
                dense = true,
            )
        }
    }
    if (shown.isEmpty() && query.isNotBlank()) {
        Text(
            stringResource(R.string.transaction_categories_nothing),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

private class Picked<T>(val value: T)

/** When [ChoiceSheet] shows its search and what it finds — pure, unit-tested. */
object ChoiceSearch {
    const val MIN_OPTIONS = 9

    fun shows(count: Int): Boolean = count >= MIN_OPTIONS

    /** Options whose label, subtitle or group contain [query], ignoring case; a blank query keeps all. */
    fun <T> filter(options: List<ChoiceOption<T>>, query: String): List<ChoiceOption<T>> {
        val q = query.trim()
        if (q.isEmpty()) return options
        return options.filter { it.label.contains(q, ignoreCase = true) || it.subtitle?.contains(q, ignoreCase = true) == true ||
                it.group?.contains(q, ignoreCase = true) == true }
    }
}
