package app.coshell.transactions.edit.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import app.coshell.R
import app.coshell.core.designsystem.components.CoShellFormSection
import app.coshell.core.designsystem.motion.CoShellExpandable
import app.coshell.core.designsystem.theme.accentInk
import app.coshell.transactions.edit.TransactionEditActions
import app.coshell.transactions.edit.TransactionEditUiState
import app.coshell.transactions.edit.leafCount

/**
 * «Категория · частые» (#290, board `Fb290A`): seven categories (three on a short phone, #818) one tap away and «Все 24» — the «Все категории»
 * sheet by folder. The chosen one is named on the right of the caption («Кафе ✓»), so a pick from «Все» is seen;
 * an automatic guess (#584) shows [GuessCaption] there instead until a manual pick.
 */
@Composable
internal fun CategoryBlock(state: TransactionEditUiState, actions: TransactionEditActions, rows: Int = 2) {
    var allOpen by rememberSaveable { mutableStateOf(false) }
    val total = state.categories.leafCount
    // [rows] of four places, the last one «Все» when something is left out (a short phone — one row, #818).
    val room = rows * GRID_COLUMNS
    val frequent = if (total > room) state.frequentCategories.take(room - 1) else state.frequentCategories.take(room)
    val hasMore = total > frequent.size
    val places = frequent.map { GridPlace.Category(it) } +
        if (hasMore) listOf(GridPlace.All(total, stringResource(R.string.transaction_categories_all)) { allOpen = true }) else emptyList()
    CoShellFormSection(
        title = stringResource(if (hasMore) R.string.entry_section_category_frequent else R.string.entry_section_category),
        trailing = state.categoryGuessSource?.takeIf { state.categoryGuessed }?.let { source -> { GuessCaption(source) } }
            ?: state.categoryName?.let { name ->
                { Text("$name ✓", style = MaterialTheme.typography.labelMedium, color = accentInk(), maxLines = 1, overflow = TextOverflow.Ellipsis) }
            },
    ) {
        if (state.categories.isEmpty()) {
            Text(
                stringResource(R.string.transaction_no_categories),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else {
            CategoryGrid(places, state.categoryId, actions.onCategorySelect)
        }
        CoShellExpandable(state.refund) {
            Text(
                state.categoryName?.let { stringResource(R.string.transaction_refund_hint, it) }
                    ?: stringResource(R.string.transaction_refund_hint_pick),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        if (state.categoryError) {
            Text(
                stringResource(R.string.transaction_category_required),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
            )
        }
    }
    if (allOpen) AllCategoriesSheet(state.categories, state.categoryId, actions.onCategorySelect, onClosed = { allOpen = false })
}
