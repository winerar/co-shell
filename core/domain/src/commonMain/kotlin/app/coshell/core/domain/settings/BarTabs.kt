package app.coshell.core.domain.settings

/**
 * A section the bottom bar can show in its second or third place (#154, poll 2026-11-27 «А · А»): «Обзор», «+» and «Ещё» stay put.
 * Saved as [name]; the order here is the order of the choice list.
 */
enum class BarTab {
    TRANSACTIONS,
    REPORTS,
    ACCOUNTS,
    BUDGETS,
    GOALS,
    DEBTS,
    ;

    companion object {
        /** Saved as [name]; unknown or missing → [default]. */
        fun parse(stored: String?, default: BarTab): BarTab = entries.firstOrNull { it.name == stored } ?: default
    }
}
