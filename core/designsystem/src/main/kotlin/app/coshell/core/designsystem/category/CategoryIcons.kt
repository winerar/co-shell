package app.coshell.core.designsystem.category

import androidx.annotation.DrawableRes
import app.coshell.core.designsystem.R

/**
 * Icon set for categories. A category stores only the stable key (e.g. `"food"`), never a
 * drawable name, so icons can be redrawn or renamed without touching data. Order = picker order.
 */
object CategoryIcons {
    private val icons: List<Pair<String, Int>> = listOf(
        "food" to R.drawable.ic_shopping_cart,
        "restaurant" to R.drawable.ic_restaurant,
        "cafe" to R.drawable.ic_local_cafe,
        "bakery" to R.drawable.ic_bakery_dining,
        "bar" to R.drawable.ic_local_bar,
        "home" to R.drawable.ic_home,
        "utilities" to R.drawable.ic_lightbulb,
        "furniture" to R.drawable.ic_chair,
        "repair" to R.drawable.ic_build,
        "construction" to R.drawable.ic_construction,
        "garden" to R.drawable.ic_local_florist,
        "cleaning" to R.drawable.ic_cleaning_services,
        "transport" to R.drawable.ic_directions_bus,
        "taxi" to R.drawable.ic_local_taxi,
        "train" to R.drawable.ic_train,
        "bike" to R.drawable.ic_pedal_bike,
        "moto" to R.drawable.ic_two_wheeler,
        "car" to R.drawable.ic_directions_car,
        "fuel" to R.drawable.ic_local_gas_station,
        "travel" to R.drawable.ic_flight,
        "hotel" to R.drawable.ic_hotel,
        "vacation" to R.drawable.ic_beach_access,
        "nature" to R.drawable.ic_park,
        "health" to R.drawable.ic_health_and_safety,
        "pharmacy" to R.drawable.ic_local_pharmacy,
        "sport" to R.drawable.ic_fitness_center,
        "beauty" to R.drawable.ic_spa,
        "haircut" to R.drawable.ic_content_cut,
        "kids" to R.drawable.ic_child_care,
        "pets" to R.drawable.ic_pets,
        "clothes" to R.drawable.ic_checkroom,
        "shopping" to R.drawable.ic_shopping_bag,
        "electronics" to R.drawable.ic_laptop_mac,
        "fun" to R.drawable.ic_celebration,
        "movies" to R.drawable.ic_movie,
        "games" to R.drawable.ic_sports_esports,
        "music" to R.drawable.ic_music_note,
        "books" to R.drawable.ic_menu_book,
        "education" to R.drawable.ic_school,
        "phone" to R.drawable.ic_mobile,
        "internet" to R.drawable.ic_wifi,
        "subscriptions" to R.drawable.ic_subscriptions,
        "gift" to R.drawable.ic_redeem,
        "charity" to R.drawable.ic_volunteer_activism,
        "documents" to R.drawable.ic_description,
        "bills" to R.drawable.ic_receipt_long,
        "bank" to R.drawable.ic_account_balance,
        "card" to R.drawable.ic_credit_card,
        "salary" to R.drawable.ic_payments,
        "work" to R.drawable.ic_work,
        "interest" to R.drawable.ic_savings,
        "other" to R.drawable.ic_category,
    )
    private val byKey: Map<String, Int> = icons.toMap()

    /** All keys, in the order the picker shows them. */
    val keys: List<String> = icons.map { it.first }

    /** Drawable for [key]; unknown or missing keys fall back to the generic category icon. */
    @DrawableRes
    fun drawableFor(key: String?): Int = key?.let(byKey::get) ?: R.drawable.ic_category
}
