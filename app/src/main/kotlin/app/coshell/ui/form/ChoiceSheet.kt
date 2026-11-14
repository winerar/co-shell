package app.coshell.ui.form

import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import app.coshell.R
import app.coshell.core.designsystem.components.CoShellListRow
import app.coshell.core.designsystem.components.CoShellSearchField
import app.coshell.ui.sheet.ModalSheet
import app.coshell.core.designsystem.R as DesignR

/** One line of a [ChoiceSheet]. */
data class ChoiceOption<T>(val value: T, val label: String, val subtitle: String? = null)

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
) {
    var picked by remember { mutableStateOf<Picked<T>?>(null) }
    var query by rememberSaveable { mutableStateOf("") }
    ModalSheet(title = title, subtitle = hint, onClosed = { picked?.let { onPick(it.value) } ?: onDismiss() }) { close ->
        if (extra != null || ChoiceSearch.shows(options.size)) {
            CoShellSearchField(
                value = query,
                onValueChange = { query = it },
                placeholder = stringResource(R.string.choice_search),
                clearLabel = stringResource(R.string.search_clear),
            )
        }
        val typed = extra?.invoke(query)?.takeIf { e -> options.none { it.value == e.value } }
        (listOfNotNull(typed) + ChoiceSearch.filter(options, query)).forEach { option ->
            CoShellListRow(
                title = option.label,
                subtitle = option.subtitle,
                trailing = if (option.value == selected) {
                    { Icon(painterResource(DesignR.drawable.ic_check), contentDescription = null) }
                } else {
                    null
                },
                onClick = {
                    picked = Picked(option.value)
                    close()
                },
            )
        }
    }
}

private class Picked<T>(val value: T)

/** When [ChoiceSheet] shows its search and what it finds — pure, unit-tested. */
object ChoiceSearch {
    const val MIN_OPTIONS = 9

    fun shows(count: Int): Boolean = count >= MIN_OPTIONS

    /** Options whose label or subtitle contain [query], ignoring case; a blank query keeps all. */
    fun <T> filter(options: List<ChoiceOption<T>>, query: String): List<ChoiceOption<T>> {
        val q = query.trim()
        if (q.isEmpty()) return options
        return options.filter { it.label.contains(q, ignoreCase = true) || it.subtitle?.contains(q, ignoreCase = true) == true }
    }
}
