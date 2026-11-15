package app.coshell.transactions.list

import app.coshell.core.domain.search.PeriodRange
import kotlinx.datetime.LocalDate

/**
 * Key in the «Операции» tab's SavedStateHandle: a [MemberPreset] in [MemberPreset.encode] form — «Отчёты» →
 * «Кто сколько потратил» → a member's operations in the report's period (#373).
 */
const val PRESET_MEMBER = "presetMember"

/**
 * «Кто внёс» = [memberId] (`null` — «Я») in [start] ..< [endExclusive]. Kept as a plain string in the
 * SavedStateHandle: `memberId|start|endExclusive|step`.
 */
data class MemberPreset(val memberId: String?, val start: LocalDate, val endExclusive: LocalDate, val step: PeriodRange.Step) {
    fun encode(): String = listOf(memberId.orEmpty(), start, endExclusive, step.name).joinToString(SEPARATOR)

    /** The filter it opens: only the author and the period, everything else cleared. */
    fun toFilter(): ListFilter = ListFilter(
        who = memberId?.let { ListFilter.Who.Member(it) } ?: ListFilter.Who.Me,
        period = PeriodRange(start, endExclusive, step),
    )

    companion object {
        private const val SEPARATOR = "|"

        /** `null` — not a preset string (nothing asked, or written by another version). */
        fun decode(text: String?): MemberPreset? {
            val parts = text?.split(SEPARATOR)?.takeIf { it.size == 4 } ?: return null
            return runCatching {
                MemberPreset(
                    memberId = parts[0].ifEmpty { null },
                    start = LocalDate.parse(parts[1]),
                    endExclusive = LocalDate.parse(parts[2]),
                    step = PeriodRange.Step.valueOf(parts[3]),
                ).takeIf { it.start < it.endExclusive }
            }.getOrNull()
        }
    }
}
