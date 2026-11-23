package app.coshell.core.domain.reports

/**
 * The encouraging words of «Итоги месяца» (#554): each result has its own phrase, the first rule that fits wins.
 * Tone — `hints.md` §«Тон»: no guilt, no red, no «exceeded». The app maps a [RecapPhrase] to a string resource.
 */
enum class RecapPhrase {
    /** Fewer than [FEW_DAYS] days with records: «Хорошее начало». */
    GOOD_START,

    /** First recorded month, nothing to compare with: «Первый месяц записан». */
    FIRST_MONTH,

    /** Spent noticeably less than the month before: «Месяц легче прошлого на N %» (see [MonthRecap.changePercent]). */
    LIGHTER,

    /** Every limit of the month stayed within: «Все лимиты в норме». */
    LIMITS_KEPT,

    /** Records for [STREAK_DAYS]+ days in a row: «N дней подряд с записями». */
    STREAK,

    /** About the same as the month before (within [SAME_PERCENT]): «Месяц почти как прошлый». */
    SAME_AS_BEFORE,

    /** Something was left of the income: «Осталось N % дохода». */
    SAVED,

    /** Spent more, no guilt: «Месяц был насыщенным, зато всё записано». */
    BUSY_BUT_RECORDED,
}

object RecapPhrases {
    const val FEW_DAYS = 5
    const val STREAK_DAYS = 7
    const val SAME_PERCENT = 3
    const val LIGHTER_PERCENT = 3

    fun pick(recap: MonthRecap): RecapPhrase {
        val change = recap.changePercent
        val saved = recap.earned.amountMinor - recap.spent.amountMinor
        return when {
            recap.recordedDays < FEW_DAYS -> RecapPhrase.GOOD_START
            change == null -> RecapPhrase.FIRST_MONTH
            change <= -LIGHTER_PERCENT -> RecapPhrase.LIGHTER
            recap.limits?.let { it.total > 0 && it.within == it.total } == true -> RecapPhrase.LIMITS_KEPT
            recap.streak >= STREAK_DAYS -> RecapPhrase.STREAK
            change in -SAME_PERCENT + 1..SAME_PERCENT -> RecapPhrase.SAME_AS_BEFORE
            saved > 0 -> RecapPhrase.SAVED
            else -> RecapPhrase.BUSY_BUT_RECORDED
        }
    }
}
