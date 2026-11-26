package app.coshell.core.domain.receipts

import kotlinx.datetime.LocalDate

/**
 * Dates as receipts print them (#742): «19/11/26», «15.11.2026», «2026-11-12», «14 de noviembre de 2026», «10 ноября 2026»,
 * «08 Nov, 2026», «15 Nov 2026». Used by [app.coshell.core.domain.ai.ReceiptAi]: the model copies the date as printed and code reads it,
 * and a date the model got wrong is replaced by one the text really has. Day first in «dd/mm» (Argentina, Armenia, Russia).
 */
object ReceiptDates {
    private val NUMERIC = Regex("""(?<!\d)(\d{1,2})[./-](\d{1,2})[./-](\d{4}|\d{2})(?!\d)""")
    private val ISO = Regex("""(?<!\d)(\d{4})-(\d{2})-(\d{2})(?!\d)""")
    private val WORDS = Regex("""(?<!\d)(\d{1,2})\s*(?:de\s+)?([A-Za-zА-Яа-яЁё]+)\.?,?\s*(?:de\s+|del\s+)?(\d{4})(?!\d)""")

    private val MONTHS: Map<String, Int> = buildMap {
        listOf(
            "янв", "фев", "мар", "апр", "мая", "июн", "июл", "авг", "сен", "окт", "ноя", "дек",
        ).forEachIndexed { i, p -> put(p, i + 1) }
        put("май", 5)
        listOf("ene", "feb", "mar", "abr", "may", "jun", "jul", "ago", "sep", "oct", "nov", "dic").forEachIndexed { i, p -> put(p, i + 1) }
        listOf("jan", "feb", "mar", "apr", "may", "jun", "jul", "aug", "sep", "oct", "nov", "dec").forEachIndexed { i, p -> put(p, i + 1) }
        put("set", 9) // «setiembre»
    }

    /** Every readable date of [text], in the order printed, without repeats. */
    fun find(text: String): List<LocalDate> {
        val found = mutableListOf<Pair<Int, LocalDate>>()
        ISO.findAll(text).forEach { m -> date(m.groupValues[1].toInt(), m.groupValues[2].toInt(), m.groupValues[3].toInt())?.let { found += m.range.first to it } }
        NUMERIC.findAll(text).forEach { m ->
            val (d, mo, y) = m.destructured
            date(year(y), mo.toInt(), d.toInt())?.let { found += m.range.first to it }
        }
        WORDS.findAll(text).forEach { m ->
            val (d, word, y) = m.destructured
            month(word)?.let { mo -> date(y.toInt(), mo, d.toInt())?.let { found += m.range.first to it } }
        }
        return found.sortedBy { it.first }.map { it.second }.distinct()
    }

    /** One date as the model copied it (any of the printed forms), `null` when it is not a date. */
    fun parse(value: String): LocalDate? = find(value).firstOrNull()

    private fun year(y: String): Int = if (y.length == 2) 2000 + y.toInt() else y.toInt()

    private fun month(word: String): Int? {
        val w = word.lowercase()
        if (w.length < 3) return null
        return MONTHS[w.take(3)]
    }

    private fun date(year: Int, month: Int, day: Int): LocalDate? =
        if (year in 2000..2099 && month in 1..12 && day in 1..31) runCatching { LocalDate(year, month, day) }.getOrNull() else null
}
