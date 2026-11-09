package app.coshell.core.domain

import app.coshell.core.model.Category
import app.coshell.core.model.CategoryType
import app.coshell.core.model.IdGenerator

/**
 * Starter set of categories, created on first launch. Users rename/archive them freely, so
 * these are data, not UI strings. Icon keys are resolved by the UI; colours are neon values of
 * the category palette (`core:designsystem` `CategoryPalette`).
 */
object DefaultCategories {
    private data class Seed(val name: String, val icon: String, val color: Long)

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
