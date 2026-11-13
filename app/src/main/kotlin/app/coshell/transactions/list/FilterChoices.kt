package app.coshell.transactions.list

import app.coshell.core.designsystem.category.CategoryPalette
import app.coshell.core.domain.tags.TagUsage
import app.coshell.core.model.Account
import app.coshell.core.model.Category
import app.coshell.core.model.CategoryType
import app.coshell.core.model.Tag
import app.coshell.core.model.buildCategoryTree

/** What the filter sheets of «Операции» list (#141, boards `Fb141ACats`, `Fb141AAcc`). */
data class FilterChoices(
    /** Live accounts in their order; archived ones only while chosen, so a filter never hides itself. */
    val accounts: List<AccountChoice> = emptyList(),
    val expenseCategories: List<CategoryChoice> = emptyList(),
    val incomeCategories: List<CategoryChoice> = emptyList(),
    /** Other family members for «Кто внёс»; empty — the row is hidden. */
    val members: List<MemberChoice> = emptyList(),
    /** Tags on live operations, by name (#294); unused ones only while chosen. Empty — the «# Тег» chip is hidden. */
    val tags: List<TagChoice> = emptyList(),
) {
    data class AccountChoice(val id: String, val name: String, val currency: String, val color: Long?)

    /** A loose category or a folder with its [children]; choosing a folder chooses [ids] — it and all of them. */
    data class CategoryChoice(
        val id: String,
        val name: String,
        val icon: String?,
        val color: Long?,
        val children: List<CategoryChoice> = emptyList(),
    ) {
        val isFolder: Boolean get() = children.isNotEmpty()
        val ids: Set<String> get() = setOf(id) + children.map { it.id }
    }

    data class MemberChoice(val id: String, val name: String)

    /** [count] — live operations with the tag; [color] — a stored palette value. */
    data class TagChoice(val id: String, val name: String, val color: Long, val count: Int)

    /**
     * [chosen] after a tap on [choice]: a folder switches itself with all its categories; a category switches
     * alone. A folder's own id stays chosen exactly while all its categories are, so operations filed right in
     * the folder follow it.
     */
    fun toggle(chosen: Set<String>, choice: CategoryChoice): Set<String> {
        val on = chosen.containsAll(choice.ids)
        var next = if (on) chosen - choice.ids else chosen + choice.ids
        folderOf[choice.id]?.let { folder ->
            val kids = folder.children.map { it.id }
            next = if (next.containsAll(kids)) next + folder.id else next - folder.id
        }
        return next
    }

    /** How many of [folder]'s categories are in [chosen]. */
    fun chosenIn(folder: CategoryChoice, chosen: Set<String>): Int = folder.children.count { it.id in chosen }

    val categoryCount: Int get() = (expenseCategories + incomeCategories).sumOf { 1 + it.children.size }

    /** Every category by id, folders and children alike — for the chip label. */
    val categoriesById: Map<String, CategoryChoice> by lazy {
        (expenseCategories + incomeCategories).flatMap { listOf(it) + it.children }.associateBy { it.id }
    }

    /** The folder each child category is in. */
    val folderOf: Map<String, CategoryChoice> by lazy {
        (expenseCategories + incomeCategories).flatMap { f -> f.children.map { it.id to f } }.toMap()
    }
}

internal fun buildFilterChoices(
    accounts: List<Account>,
    categories: List<Category>,
    authors: Map<String, String>,
    filter: ListFilter,
    tags: List<Tag> = emptyList(),
    tagUsage: Map<String, TagUsage> = emptyMap(),
): FilterChoices {
    fun tree(type: CategoryType) = buildCategoryTree(
        categories.filter { it.type == type && (!it.archived || it.id in filter.categoryIds) },
    ).map { node ->
        FilterChoices.CategoryChoice(
            id = node.category.id,
            name = node.category.name,
            icon = node.category.icon,
            color = node.category.color,
            children = node.children.map { FilterChoices.CategoryChoice(it.id, it.name, it.icon, it.color) },
        )
    }
    return FilterChoices(
        accounts = accounts
            .filter { !it.archived || it.id in filter.accountIds }
            .map { FilterChoices.AccountChoice(it.id, it.name, it.currency.value, it.color) },
        expenseCategories = tree(CategoryType.EXPENSE),
        incomeCategories = tree(CategoryType.INCOME),
        members = authors.map { (id, name) -> FilterChoices.MemberChoice(id, name) }.sortedBy { it.name },
        tags = tags
            .filter { (tagUsage[it.id]?.count ?: 0) > 0 || it.id in filter.tagIds }
            .sortedBy { it.name.lowercase() }
            .map { FilterChoices.TagChoice(it.id, it.name, it.color ?: CategoryPalette.forKey(it.id), tagUsage[it.id]?.count ?: 0) },
    )
}
