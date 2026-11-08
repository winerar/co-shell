package app.coshell.core.domain

import app.coshell.core.model.Category
import app.coshell.core.model.CategoryType
import app.coshell.core.model.IdGenerator

/**
 * Starter set of categories, created on first launch. Users rename/archive them freely, so
 * these are data, not UI strings. Icon keys are resolved by the UI.
 */
object DefaultCategories {
    private data class Seed(val name: String, val icon: String, val color: Long)

    private val expenses = listOf(
        Seed("Продукты", "food", 0xFF4CAF50),
        Seed("Рестораны и кафе", "restaurant", 0xFFFF9800),
        Seed("Дом", "home", 0xFF795548),
        Seed("Коммунальные услуги", "utilities", 0xFF607D8B),
        Seed("Транспорт", "transport", 0xFF2196F3),
        Seed("Такси", "taxi", 0xFFFFC107),
        Seed("Здоровье", "health", 0xFFE91E63),
        Seed("Дети", "kids", 0xFF9C27B0),
        Seed("Одежда и обувь", "clothes", 0xFF3F51B5),
        Seed("Развлечения", "fun", 0xFFFF5722),
        Seed("Образование", "education", 0xFF009688),
        Seed("Связь и подписки", "subscriptions", 0xFF00BCD4),
        Seed("Подарки", "gift", 0xFFF44336),
        Seed("Документы и налоги", "documents", 0xFF9E9E9E),
        Seed("Другое", "other", 0xFF757575),
    )

    private val incomes = listOf(
        Seed("Зарплата", "salary", 0xFF4CAF50),
        Seed("Проценты и дивиденды", "interest", 0xFF8BC34A),
        Seed("Подарки", "gift", 0xFFF44336),
        Seed("Другое", "other", 0xFF757575),
    )

    fun create(ids: IdGenerator): List<Category> =
        expenses.toCategories(CategoryType.EXPENSE, ids) + incomes.toCategories(CategoryType.INCOME, ids)

    private fun List<Seed>.toCategories(type: CategoryType, ids: IdGenerator) =
        mapIndexed { index, seed ->
            Category(
                id = ids.newId(),
                name = seed.name,
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
) {
    suspend operator fun invoke(): Boolean {
        if (categories.countCategories() > 0) return false
        categories.saveCategories(DefaultCategories.create(ids))
        return true
    }
}
