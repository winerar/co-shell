package app.coshell.core.domain.settings

/** The cards of the «Обзор» tab of «Отчёты» (#826), in the order the feed had before it could be changed. */
enum class ReportCard {
    /** «На что ушли деньги». */
    SPENDING,

    /** «Необычное за …» (#787). */
    UNUSUAL,

    /** «Оплата в другой валюте» (#786). */
    CONVERSION,

    /** «По месяцам». */
    MONTHS,

    /** «Траты в ARS и в USD»: the personal inflation (#788). */
    INFLATION,

    /** «Быстрее всего росли». */
    INFLATION_GROWTH,

    /** «Доходы и расходы». */
    INCOME,

    /** «Капитал». */
    CAPITAL,

    /** «Подушка» (#241). */
    CUSHION,

    /** «Прогноз остатка» (#770). */
    FORECAST,

    /** «Постоянные траты» (#771). */
    FIXED,

    /** «Кто сколько потратил». */
    MEMBERS,
}

/**
 * Which cards «Отчёты» show and in what order — editing right on the feed (hold a card) and «Настройки → Экраны → Отчёты»
 * (#826, poll 2026-11-28 «А», board `Rep826Compact`). Per phone. A card the app gets later is unknown to a saved layout: it joins
 * at the end, shown.
 */
data class ReportLayout(
    val order: List<ReportCard> = ReportCard.entries,
    val hidden: Set<ReportCard> = emptySet(),
) {
    /** The cards to draw, in order. */
    val visible: List<ReportCard> get() = order.filter { it !in hidden }

    /** The hidden cards, in [order]: «Скрыты» under the list in the editing mode. */
    val hiddenCards: List<ReportCard> get() = order.filter { it in hidden }

    fun isShown(card: ReportCard): Boolean = card !in hidden

    /** Shown again — it comes back at the end of the shown cards; hidden — it leaves them where it was. */
    fun withShown(card: ReportCard, shown: Boolean): ReportLayout = when {
        shown && card in hidden -> ReportLayout(visible + card + (hiddenCards - card), hidden - card)
        !shown -> copy(hidden = hidden + card)
        else -> this
    }

    /** [card] goes to place [to] among the [visible] ones (a drop of the editing list); out of range or hidden — nothing changes. */
    fun moveShown(card: ReportCard, to: Int): ReportLayout {
        val shown = visible
        val from = shown.indexOf(card)
        if (from < 0 || to !in shown.indices || from == to) return this
        val list = shown.toMutableList()
        list.add(to, list.removeAt(from))
        return copy(order = list + hiddenCards)
    }

    /** `"MONTHS,SPENDING|MEMBERS"` — [order] and, after the bar, [hidden]. */
    fun encode(): String = order.joinToString(",") { it.name } + "|" + hidden.joinToString(",") { it.name }

    companion object {
        /** Reads [encode]; unknown names are dropped, missing cards join the end (shown), bad text gives the default. */
        fun parse(stored: String?): ReportLayout {
            if (stored.isNullOrBlank()) return ReportLayout()
            val (orderPart, hiddenPart) = stored.split('|', limit = 2).let { it[0] to it.getOrElse(1) { "" } }
            fun cards(text: String) = text.split(',').mapNotNull { name -> ReportCard.entries.firstOrNull { it.name == name } }
            val order = cards(orderPart).distinct().let { known -> known + ReportCard.entries.filter { it !in known } }
            return ReportLayout(order, cards(hiddenPart).toSet())
        }
    }
}
