package app.coshell.core.model

import kotlinx.datetime.LocalDate

/**
 * A savings goal (#768, poll 2026-11-26 «А»): what is saved is the balance of [accountId] — no separate «отложить».
 * Several goals on one account share its balance by deadline (`core:domain/goals/GoalBook`). [target] is in the account's currency.
 * A [yearly] goal (insurance, taxes) starts a new round each year on the day of [deadline]; it needs a deadline.
 */
data class Goal(
    val id: String,
    val name: String,
    val target: Money,
    val accountId: String,
    val deadline: LocalDate? = null,
    val yearly: Boolean = false,
    /** Icon key from the category icon set (`CategoryIcons`); null — the screen's default. */
    val icon: String? = null,
    val color: Long? = null,
) {
    init {
        require(name.isNotBlank()) { "A goal needs a name" }
        require(target.isPositive) { "A goal target must be positive" }
        require(!yearly || deadline != null) { "A yearly goal needs a deadline" }
    }
}
