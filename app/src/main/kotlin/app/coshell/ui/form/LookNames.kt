package app.coshell.ui.form

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import app.coshell.R
import app.coshell.core.designsystem.category.CategoryIcons
import app.coshell.core.designsystem.category.CategoryPalette

// Spoken names (TalkBack) of palette colours and category icons (#462); the unit test checks that every one has a name.

@StringRes
internal fun colorNameRes(color: Long): Int =
    COLOR_NAMES.getOrElse(CategoryPalette.colors.indexOf(CategoryPalette.normalize(color))) { R.string.category_color }

@StringRes
internal fun iconNameRes(key: String): Int = ICON_NAMES.getValue(key)

/** The spoken name of a palette colour in the current language. */
@Composable
internal fun rememberColorLabel(): (Long) -> String {
    LocalConfiguration.current // a language change re-reads the names
    val resources = LocalContext.current.resources
    return { color -> resources.getString(colorNameRes(color)) }
}

/** The spoken name of a category icon in the current language. */
@Composable
internal fun rememberIconLabel(): (String) -> String {
    LocalConfiguration.current
    val resources = LocalContext.current.resources
    return { key -> resources.getString(iconNameRes(key)) }
}

internal val COLOR_NAMES: List<Int> = listOf(
    R.string.color_name_red,
    R.string.color_name_coral,
    R.string.color_name_orange,
    R.string.color_name_peach,
    R.string.color_name_caramel,
    R.string.color_name_amber,
    R.string.color_name_sand,
    R.string.color_name_yellow,
    R.string.color_name_lime,
    R.string.color_name_sage,
    R.string.color_name_green,
    R.string.color_name_seafoam,
    R.string.color_name_teal,
    R.string.color_name_cyan,
    R.string.color_name_sky,
    R.string.color_name_steel,
    R.string.color_name_periwinkle,
    R.string.color_name_indigo,
    R.string.color_name_violet,
    R.string.color_name_lavender,
    R.string.color_name_magenta,
    R.string.color_name_mauve,
    R.string.color_name_pink,
    R.string.color_name_blush,
)

internal val ICON_NAMES: Map<String, Int> = mapOf(
    "food" to R.string.icon_name_food,
    "restaurant" to R.string.icon_name_restaurant,
    "cafe" to R.string.icon_name_cafe,
    "bakery" to R.string.icon_name_bakery,
    "bar" to R.string.icon_name_bar,
    "home" to R.string.icon_name_home,
    "utilities" to R.string.icon_name_utilities,
    "furniture" to R.string.icon_name_furniture,
    "repair" to R.string.icon_name_repair,
    "construction" to R.string.icon_name_construction,
    "garden" to R.string.icon_name_garden,
    "cleaning" to R.string.icon_name_cleaning,
    "transport" to R.string.icon_name_transport,
    "taxi" to R.string.icon_name_taxi,
    "train" to R.string.icon_name_train,
    "bike" to R.string.icon_name_bike,
    "moto" to R.string.icon_name_moto,
    "car" to R.string.icon_name_car,
    "fuel" to R.string.icon_name_fuel,
    "travel" to R.string.icon_name_travel,
    "hotel" to R.string.icon_name_hotel,
    "vacation" to R.string.icon_name_vacation,
    "nature" to R.string.icon_name_nature,
    "health" to R.string.icon_name_health,
    "pharmacy" to R.string.icon_name_pharmacy,
    "sport" to R.string.icon_name_sport,
    "beauty" to R.string.icon_name_beauty,
    "haircut" to R.string.icon_name_haircut,
    "kids" to R.string.icon_name_kids,
    "pets" to R.string.icon_name_pets,
    "clothes" to R.string.icon_name_clothes,
    "shopping" to R.string.icon_name_shopping,
    "electronics" to R.string.icon_name_electronics,
    "fun" to R.string.icon_name_fun,
    "movies" to R.string.icon_name_movies,
    "games" to R.string.icon_name_games,
    "music" to R.string.icon_name_music,
    "books" to R.string.icon_name_books,
    "education" to R.string.icon_name_education,
    "phone" to R.string.icon_name_phone,
    "internet" to R.string.icon_name_internet,
    "subscriptions" to R.string.icon_name_subscriptions,
    "gift" to R.string.icon_name_gift,
    "charity" to R.string.icon_name_charity,
    "documents" to R.string.icon_name_documents,
    "bills" to R.string.icon_name_bills,
    "bank" to R.string.icon_name_bank,
    "card" to R.string.icon_name_card,
    "salary" to R.string.icon_name_salary,
    "work" to R.string.icon_name_work,
    "interest" to R.string.icon_name_interest,
    "other" to R.string.icon_name_other,
)
