package app.coshell.core.model

data class Category(
    val id: String,
    val name: String,
    val type: CategoryType,
    /** Icon key from the app's icon set (resolved in the UI layer), e.g. `"food"`. */
    val icon: String? = null,
    /** ARGB colour, e.g. `0xFF4CAF50`. */
    val color: Long? = null,
    /** Parent category for subcategories; only one level of nesting is used. */
    val parentId: String? = null,
    val archived: Boolean = false,
    val sortOrder: Int = 0,
) {
    init {
        require(name.isNotBlank()) { "Category name must not be blank" }
    }
}

enum class CategoryType { EXPENSE, INCOME }
