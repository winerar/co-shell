package app.coshell.drafts.list

import app.coshell.core.domain.drafts.DraftMissing
import app.coshell.core.model.DraftCategorySource
import kotlinx.datetime.LocalDate

/**
 * One draft as the row shows it: [amount] is ready text (`null` — no number), [missing] what is still needed (`null` — ready),
 * [checked] — ticked: «Записать» posts the ticked ready ones, «Удалить» removes every ticked one (#628).
 */
data class DraftRowUi(
    val id: String,
    val title: String,
    val accountName: String?,
    val date: LocalDate,
    val amount: String?,
    val missing: DraftMissing?,
    val categoryName: String?,
    val source: DraftCategorySource?,
    val checked: Boolean,
) {
    val ready: Boolean get() = missing == null
}

data class DraftsUiState(
    val isLoading: Boolean = true,
    val rows: List<DraftRowUi> = emptyList(),
    val today: LocalDate = LocalDate(1970, 1, 1),
    /** The person changed the ticks: only then «Удалить · N» stands beside «Записать» (boards `Notes586Drafts` vs `Notes628Delete`). */
    val ticksTouched: Boolean = false,
) {
    val readyCount: Int get() = rows.count { it.ready }
    val postCount: Int get() = rows.count { it.checked && it.ready }
    val deleteCount: Int get() = if (ticksTouched) rows.count { it.checked } else 0
    val isEmpty: Boolean get() = !isLoading && rows.isEmpty()
}
