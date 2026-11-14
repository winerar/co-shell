package app.coshell.categories

import android.content.Context
import app.coshell.R
import app.coshell.core.domain.CategoryNames

/**
 * Names of the categories the app creates itself, from `strings_defaults.xml` — so a new language gets
 * its own starter set. An explicit map, not `getIdentifier`: the resource shrinker keeps what is referenced.
 */
class ResourceCategoryNames(private val context: Context) : CategoryNames {
    override fun name(key: String): String? = IDS[key]?.let(context::getString)

    companion object {
        val IDS: Map<String, Int> = mapOf(
            "expense.food" to R.string.default_category_expense_food,
            "expense.restaurant" to R.string.default_category_expense_restaurant,
            "expense.home" to R.string.default_category_expense_home,
            "expense.utilities" to R.string.default_category_expense_utilities,
            "expense.transport" to R.string.default_category_expense_transport,
            "expense.taxi" to R.string.default_category_expense_taxi,
            "expense.health" to R.string.default_category_expense_health,
            "expense.kids" to R.string.default_category_expense_kids,
            "expense.clothes" to R.string.default_category_expense_clothes,
            "expense.fun" to R.string.default_category_expense_fun,
            "expense.education" to R.string.default_category_expense_education,
            "expense.subscriptions" to R.string.default_category_expense_subscriptions,
            "expense.gift" to R.string.default_category_expense_gift,
            "expense.documents" to R.string.default_category_expense_documents,
            "expense.other" to R.string.default_category_expense_other,
            "income.salary" to R.string.default_category_income_salary,
            "income.interest" to R.string.default_category_income_interest,
            "income.gift" to R.string.default_category_income_gift,
            "income.other" to R.string.default_category_income_other,
            CategoryNames.ADJUSTMENT to R.string.default_category_adjustment,
        )
    }
}
