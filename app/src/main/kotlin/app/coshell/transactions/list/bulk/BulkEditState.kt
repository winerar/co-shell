package app.coshell.transactions.list.bulk

import app.coshell.core.designsystem.category.CategoryPalette
import app.coshell.core.domain.bulk.BulkEditRules
import app.coshell.core.model.Category
import app.coshell.core.model.CategoryType
import app.coshell.core.model.Tag
import app.coshell.core.model.Transaction
import app.coshell.ui.form.CategoryLine

/**
 * What the «Изменить» sheets of the selection mode show (#794, boards `Bulk794*`): [step] is the open sheet, [count] the chosen
 * rows. [categories] is empty when «Категория» stays off ([categoryAllowed] — the rows are not all expenses or all incomes).
 */
internal data class BulkEditState(
    val step: Step = Step.MENU,
    val count: Int,
    val categoryAllowed: Boolean,
    val categories: List<CategoryLine> = emptyList(),
    val tags: List<BulkTag> = emptyList(),
) {
    enum class Step { MENU, CATEGORY, TAGS }

    /** A tag to add: [color] is already resolved (own or from the palette). */
    data class BulkTag(val id: String, val name: String, val color: Long)
}

/** Builds [BulkEditState] for the chosen [rows]: categories of the one type they share, live tags by name. */
internal fun buildBulkEdit(rows: List<Transaction>, categories: List<Category>, tags: List<Tag>): BulkEditState {
    val type: CategoryType? = BulkEditRules.categoryType(rows)
    val live = categories.filter { !it.archived && it.type == type }
    val byId = live.associateBy { it.id }
    val lines = live.sortedBy { it.sortOrder }.map { c ->
        val parent = c.parentId?.let(byId::get)
        CategoryLine(c.id, c.name, parent?.name, c.icon, c.color, parent?.id)
    }
    return BulkEditState(
        count = rows.size,
        categoryAllowed = type != null,
        categories = lines,
        tags = tags.sortedBy { it.name.lowercase() }
            .map { BulkEditState.BulkTag(it.id, it.name, it.color ?: CategoryPalette.forKey(it.id)) },
    )
}
