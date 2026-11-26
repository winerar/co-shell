package app.coshell.goals.edit

import androidx.compose.runtime.Immutable
import app.coshell.core.designsystem.components.RibbonAccount
import app.coshell.ui.form.ChoiceOption

/** Error of a field of the goal sheet; null — fine. */
enum class GoalError { NAME, TARGET, ACCOUNT, YEARLY }

/** Where an existing goal stands, at the top of its sheet (board `GoalDetail`). */
@Immutable
data class GoalProgressUi(val saved: String, val target: String, val left: String, val fraction: Float, val reached: Boolean)

/** The goal sheet (#768, boards `GoalNew`, `GoalDetail`): name, target, deadline, account, «Каждый год», the plan a month. */
@Immutable
data class GoalEditUiState(
    val isNew: Boolean = true,
    val isLoading: Boolean = true,
    val name: String = "",
    val amount: String = "",
    /** Currency code of the chosen account: the target is in it. */
    val currency: String? = null,
    val accountId: String? = null,
    val ribbon: List<RibbonAccount> = emptyList(),
    val accounts: List<ChoiceOption<String>> = emptyList(),
    val deadlineMillisUtc: Long? = null,
    val deadlineText: String? = null,
    val yearly: Boolean = false,
    /** «≈ 125 USD» and «1 июля» for «Откладывайте … в месяц, чтобы успеть к …»; empty — no plan to show. */
    val planPerMonth: String = "",
    val planDeadline: String = "",
    val planReached: Boolean = false,
    val progress: GoalProgressUi? = null,
    /** What «Пополнить» puts into the transfer, minor units of the goal's currency (0 — nothing to suggest). */
    val topUpMinor: Long = 0,
    val errors: Set<GoalError> = emptySet(),
    val isDone: Boolean = false,
)
