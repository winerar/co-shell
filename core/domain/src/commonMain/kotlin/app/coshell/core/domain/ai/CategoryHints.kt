package app.coshell.core.domain.ai

import app.coshell.core.model.Category
import app.coshell.core.model.CategoryType

/**
 * What the person's categories hold, for a small model (#742): it put a haircut into «Образование» and a kindergarten into «Дом» because
 * a bare name says little. Only categories with a starter icon get a hint (the icon says what it is, whatever the name or language);
 * the person's own categories go by name alone. Words in English: the model reads them best, and they are not shown to anyone.
 */
object CategoryHints {
    private val BY_ICON = mapOf(
        "food" to "groceries, supermarket, bakery, market",
        "restaurant" to "cafe, restaurant, delivery food, coffee",
        "home" to "household goods, repairs, furniture, laundry",
        "utilities" to "electricity, gas, water, internet bills",
        "transport" to "fuel, public transport card, parking, bike, car service",
        "taxi" to "taxi, ride apps",
        "health" to "pharmacy, doctor, dentist, medical tests",
        "kids" to "kindergarten, toys, children's things",
        "clothes" to "clothes, shoes",
        "fun" to "cinema, games, hobbies, sport, gym",
        "education" to "courses, books, school",
        "subscriptions" to "phone plan, streaming, subscriptions",
        "gift" to "gifts, flowers",
        "documents" to "taxes, fees for documents",
        "other" to "haircut, beauty, pets, bank fees, anything else",
    )

    /** One prompt line `What the categories hold: Name — words; …`, empty when no category has a hint. */
    fun line(categories: List<Category>): String {
        val parts = categories.filter { it.type == CategoryType.EXPENSE && !it.archived }
            .mapNotNull { c -> BY_ICON[c.icon]?.let { "${c.name} — $it" } }
        return if (parts.isEmpty()) "" else "What the categories hold: " + parts.joinToString("; ")
    }
}
