package app.coshell.categories

import androidx.annotation.StringRes
import app.coshell.R
import app.coshell.core.model.CategoryType

@get:StringRes
val CategoryType.labelRes: Int
    get() = when (this) {
        CategoryType.EXPENSE -> R.string.category_type_expense
        CategoryType.INCOME -> R.string.category_type_income
    }
