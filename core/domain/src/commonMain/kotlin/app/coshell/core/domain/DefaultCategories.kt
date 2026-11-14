package app.coshell.core.domain

import app.coshell.core.model.Category
import app.coshell.core.model.CategoryType
import app.coshell.core.model.IdGenerator

/**
 * Starter set of categories, created on first launch in the app's language (`ru`, `en`, `es`;
 * anything else → Russian). Users rename/archive them freely, so these are data, not UI strings:
 * once created, a name stays as it is when the language changes. Icon keys are resolved by the UI; colours are neon values of
 * the category palette (`core:designsystem` `CategoryPalette`).
 */
object DefaultCategories {
    private data class Seed(val ru: String, val en: String, val es: String, val icon: String, val color: Long) {
        fun name(language: String): String = when (language) {
            "en" -> en
            "es" -> es
            else -> ru
        }
    }

    private val expenses = listOf(
        Seed("Продукты", "Groceries", "Supermercado", "food", 0xFF4DFF88),
        Seed("Рестораны и кафе", "Restaurants & cafés", "Restaurantes y cafés", "restaurant", 0xFFFFA53D),
        Seed("Дом", "Home", "Hogar", "home", 0xFFFFB38A),
        Seed("Коммунальные услуги", "Utilities", "Suministros", "utilities", 0xFF8FB3CC),
        Seed("Транспорт", "Transport", "Transporte", "transport", 0xFF38B6FF),
        Seed("Такси", "Taxi", "Taxi", "taxi", 0xFFFFD43B),
        Seed("Здоровье", "Health", "Salud", "health", 0xFFFF4FD8),
        Seed("Дети", "Kids", "Niños", "kids", 0xFFD36BFF),
        Seed("Одежда и обувь", "Clothes & shoes", "Ropa y calzado", "clothes", 0xFF6E8BFF),
        Seed("Развлечения", "Entertainment", "Ocio", "fun", 0xFFFF7A3D),
        Seed("Образование", "Education", "Educación", "education", 0xFF00E5C0),
        Seed("Связь и подписки", "Phone & subscriptions", "Teléfono y suscripciones", "subscriptions", 0xFF2BE4FF),
        Seed("Подарки", "Gifts", "Regalos", "gift", 0xFFFF5470),
        Seed("Документы и налоги", "Documents & taxes", "Trámites e impuestos", "documents", 0xFFB0B8C4),
        Seed("Другое", "Other", "Otros", "other", 0xFF7A8494),
    )

    private val incomes = listOf(
        Seed("Зарплата", "Salary", "Salario", "salary", 0xFF4DFF88),
        Seed("Проценты и дивиденды", "Interest & dividends", "Intereses y dividendos", "interest", 0xFFA8FF3E),
        Seed("Подарки", "Gifts", "Regalos", "gift", 0xFFFF5470),
        Seed("Другое", "Other", "Otros", "other", 0xFF7A8494),
    )

    fun create(ids: IdGenerator, language: String = "ru"): List<Category> =
        expenses.toCategories(CategoryType.EXPENSE, ids, language) + incomes.toCategories(CategoryType.INCOME, ids, language)

    private fun List<Seed>.toCategories(type: CategoryType, ids: IdGenerator, language: String) =
        mapIndexed { index, seed ->
            Category(
                id = ids.newId(),
                name = seed.name(language),
                type = type,
                icon = seed.icon,
                color = seed.color,
                sortOrder = index,
            )
        }
}

/**
 * Creates [DefaultCategories] if the user has no categories at all (first launch).
 * @property language the app's language tag at the moment of seeding (`ru`, `en`, `es`).
 */
class SeedDefaultCategoriesUseCase(
    private val categories: CategoryRepository,
    private val ids: IdGenerator,
    private val language: () -> String = { "ru" },
) {
    suspend operator fun invoke(): Boolean {
        if (categories.countCategories() > 0) return false
        categories.saveCategories(DefaultCategories.create(ids, language()))
        return true
    }
}
