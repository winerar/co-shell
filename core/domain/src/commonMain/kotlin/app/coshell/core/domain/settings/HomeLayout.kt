package app.coshell.core.domain.settings

/** The cards of «Обзор» below the balance (which is always first and cannot be hidden). */
enum class HomeCard {
    /** «Итог месяца» in the first days of a month. */
    REVIEW,

    /** «Скоро»: regular payments of the next days. */
    UPCOMING,

    /** The tiles: spending of the month and the budget. */
    TILES,

    /** «Горит в бюджете»: limits under threat. */
    HOT,

    /** «Последние операции». */
    RECENT,
}

/**
 * Which cards «Обзор» shows and in what order — «Настройки → Экраны → Обзор» (board `Set1Home`). Per phone. A card the app gets
 * later is unknown to a saved layout: it joins at the end, shown.
 */
data class HomeLayout(
    val order: List<HomeCard> = HomeCard.entries,
    val hidden: Set<HomeCard> = emptySet(),
) {
    /** The cards to draw, in order. */
    val visible: List<HomeCard> get() = order.filter { it !in hidden }

    fun isShown(card: HomeCard): Boolean = card !in hidden

    fun withShown(card: HomeCard, shown: Boolean): HomeLayout = copy(hidden = if (shown) hidden - card else hidden + card)

    /** The card at [from] goes to place [to] (both indices of [order]); out of range — nothing changes. */
    fun move(from: Int, to: Int): HomeLayout {
        if (from !in order.indices || to !in order.indices || from == to) return this
        val list = order.toMutableList()
        list.add(to, list.removeAt(from))
        return copy(order = list)
    }

    /** «5 карточек»: how many are shown. */
    val shownCount: Int get() = visible.size

    /** `"UPCOMING,RECENT|HOT"` — [order] and, after the bar, [hidden]. */
    fun encode(): String = order.joinToString(",") { it.name } + "|" + hidden.joinToString(",") { it.name }

    companion object {
        /** Reads [encode]; unknown names are dropped, missing cards join the end (shown), bad text gives the default. */
        fun parse(stored: String?): HomeLayout {
            if (stored.isNullOrBlank()) return HomeLayout()
            val (orderPart, hiddenPart) = stored.split('|', limit = 2).let { it[0] to it.getOrElse(1) { "" } }
            fun cards(text: String) = text.split(',').mapNotNull { name -> HomeCard.entries.firstOrNull { it.name == name } }
            val order = cards(orderPart).distinct().let { known -> known + HomeCard.entries.filter { it !in known } }
            return HomeLayout(order, cards(hiddenPart).toSet())
        }
    }
}
