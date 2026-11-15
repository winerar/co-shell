package app.coshell.core.domain

import app.coshell.core.model.Category
import app.coshell.core.model.CategoryType
import app.coshell.core.model.IdGenerator

/**
 * Names of the categories the app creates itself, in the interface language. The app reads them from
 * string resources; a `null` keeps the built-in Russian name (tests, and keys the app does not know).
 * Keys: [DefaultCategories.key] for the starter set, [ADJUSTMENT] for «Корректировка баланса».
 */
fun interface CategoryNames {
    fun name(key: String): String?

    companion object {
        const val ADJUSTMENT = "system.adjustment"

        /** Built-in Russian names. */
        val BuiltIn = CategoryNames { null }
    }
}

/**
 * Starter set of categories, created on first launch. Once created they are user data (renamed and
 * archived freely), so their names are localized only at creation, through [CategoryNames]. Icon keys
 * are resolved by the UI; colours are neon values of the category palette (`core:designsystem` `CategoryPalette`).
 */
object DefaultCategories {
    private data class Seed(val name: String, val icon: String, val color: Long)

    /** Key of a starter category in [CategoryNames]: `expense.food`, `income.other`… */
    fun key(type: CategoryType, icon: String): String = "${type.name.lowercase()}.$icon"

    /** Name of the starter category of [type] with [icon] in [names]; `null` if the starter set has none. */
    fun name(type: CategoryType, icon: String, names: CategoryNames): String? =
        (if (type == CategoryType.EXPENSE) expenses else incomes).firstOrNull { it.icon == icon }
            ?.let { seed -> names.name(key(type, icon))?.takeIf { it.isNotBlank() } ?: seed.name }

    /** Every key of the starter set, for checking that the app has a string for each. */
    val keys: List<String> get() =
        expenses.map { key(CategoryType.EXPENSE, it.icon) } + incomes.map { key(CategoryType.INCOME, it.icon) }

    private val expenses = listOf(
        Seed("Продукты", "food", 0xFF4DFF88),
        Seed("Рестораны и кафе", "restaurant", 0xFFFFA53D),
        Seed("Дом", "home", 0xFFFFB38A),
        Seed("Коммунальные услуги", "utilities", 0xFF8FB3CC),
        Seed("Транспорт", "transport", 0xFF38B6FF),
        Seed("Такси", "taxi", 0xFFFFD43B),
        Seed("Здоровье", "health", 0xFFFF4FD8),
        Seed("Дети", "kids", 0xFFD36BFF),
        Seed("Одежда и обувь", "clothes", 0xFF6E8BFF),
        Seed("Развлечения", "fun", 0xFFFF7A3D),
        Seed("Образование", "education", 0xFF00E5C0),
        Seed("Связь и подписки", "subscriptions", 0xFF2BE4FF),
        Seed("Подарки", "gift", 0xFFFF5470),
        Seed("Документы и налоги", "documents", 0xFFB0B8C4),
        Seed("Другое", "other", 0xFF7A8494),
    )

    private val incomes = listOf(
        Seed("Зарплата", "salary", 0xFF4DFF88),
        Seed("Проценты и дивиденды", "interest", 0xFFA8FF3E),
        Seed("Подарки", "gift", 0xFFFF5470),
        Seed("Другое", "other", 0xFF7A8494),
    )

    fun create(ids: IdGenerator, names: CategoryNames = CategoryNames.BuiltIn): List<Category> =
        expenses.toCategories(CategoryType.EXPENSE, ids, names) + incomes.toCategories(CategoryType.INCOME, ids, names)

    private fun List<Seed>.toCategories(type: CategoryType, ids: IdGenerator, names: CategoryNames) =
        mapIndexed { index, seed ->
            Category(
                id = ids.newId(),
                name = names.name(key(type, seed.icon))?.takeIf { it.isNotBlank() } ?: seed.name,
                type = type,
                icon = seed.icon,
                color = seed.color,
                sortOrder = index,
            )
        }
}

/** Creates [DefaultCategories] if the user has no categories at all (first launch). */
class SeedDefaultCategoriesUseCase(
    private val categories: CategoryRepository,
    private val ids: IdGenerator,
    private val names: CategoryNames = CategoryNames.BuiltIn,
) {
    suspend operator fun invoke(): Boolean {
        if (categories.countCategories() > 0) return false
        categories.saveCategories(DefaultCategories.create(ids, names))
        return true
    }
}
