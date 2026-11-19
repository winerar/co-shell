package app.coshell.core.domain.hints

/** The «Первые шаги» list (`hints.md`); order is the order on the sheet. */
enum class FirstStep { ACCOUNT, ENTRY, LIMIT, FAMILY }

/** Facts from the data: a step is ticked by what exists, not by a tap. */
data class FirstStepsData(val hasAccount: Boolean, val hasEntry: Boolean, val hasLimit: Boolean, val hasPartner: Boolean)

data class FirstStepsState(val done: Set<FirstStep>, val hidden: Boolean) {
    val doneCount: Int get() = done.size
    val total: Int get() = FirstStep.entries.size

    /** The next step to suggest; null when all are done. */
    val next: FirstStep? get() = FirstStep.entries.firstOrNull { it !in done }

    /** Account, entry and limit are enough: «Позовите близкого человека» stays optional. */
    val complete: Boolean get() = FirstStep.ACCOUNT in done && FirstStep.ENTRY in done && FirstStep.LIMIT in done

    /** The row on «Обзор» shows until the person hides it or finishes. */
    val visible: Boolean get() = !hidden && !complete

    companion object {
        fun of(data: FirstStepsData, hidden: Boolean): FirstStepsState {
            val done = buildSet {
                if (data.hasAccount) add(FirstStep.ACCOUNT)
                if (data.hasEntry) add(FirstStep.ENTRY)
                if (data.hasLimit) add(FirstStep.LIMIT)
                if (data.hasPartner) add(FirstStep.FAMILY)
            }
            return FirstStepsState(done, hidden)
        }
    }
}
