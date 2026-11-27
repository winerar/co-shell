package app.coshell.core.domain.reports

/** One row of «Необычное в ноябре» (#787): a folder (or a category without one) and how it differs from usual. */
data class UnusualRow(val categoryId: String, val mark: UsualMark)

/** Rows the card shows at most: a short list is read, a long one is skipped. */
const val UNUSUAL_TOP = 3

/**
 * A mark is worth its own line only from this difference (a [UsualMark] starts at [MARK_MIN_PERCENT]): «на 30 % меньше» or
 * more — or new spending, nothing usually spent there.
 */
const val UNUSUAL_MIN_PERCENT = 30

/**
 * «Необычное» (#787, poll 2026-11-27 «А», board `Smart787Card`): the [top] most different folders out of [marks] ([usualMarks]),
 * by the size of the difference in money, larger first. Smaller than [UNUSUAL_MIN_PERCENT] — left out; empty — no card.
 */
fun unusualSpending(marks: Map<String, UsualMark>, top: Int = UNUSUAL_TOP): List<UnusualRow> =
    marks.entries
        .filter { (_, mark) -> mark.percent.let { it == null || kotlin.math.abs(it) >= UNUSUAL_MIN_PERCENT } }
        .sortedWith(compareByDescending<Map.Entry<String, UsualMark>> { it.value.difference.abs().amountMinor }.thenBy { it.key })
        .take(top)
        .map { UnusualRow(it.key, it.value) }
