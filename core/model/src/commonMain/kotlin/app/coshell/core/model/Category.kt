package app.coshell.core.model

/** Expense or income category; [parentId] makes a two-level tree (Food → Restaurants). */
data class Category(
    val id: CategoryId,
    val name: String,
    val type: CategoryType,
    /** Icon key resolved by the UI (e.g. `"cart"`), not a resource id. */
    val icon: String = "",
    /** Color as 0xAARRGGBB. */
    val colorArgb: Long = 0xFF9E9E9E,
    val parentId: CategoryId? = null,
    val meta: SyncMeta,
) {
    init {
        require(name.isNotBlank()) { "Category name must not be blank" }
        require(parentId != id) { "Category cannot be its own parent" }
        require(colorArgb in 0..0xFFFFFFFF) { "Color must fit 0xAARRGGBB" }
    }
}

enum class CategoryType { EXPENSE, INCOME }
