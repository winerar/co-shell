package app.coshell.debts.edit

import androidx.compose.runtime.Immutable
import app.coshell.ui.form.ChoiceOption

/** The new-debt sheet (#552): direction, person, amount, the account it moves, an optional return day and a note. */
@Immutable
data class DebtNewUiState(
    /** 0 — «Мне должны» (I lend), 1 — «Я должен» (I borrow). */
    val direction: Int = 0,
    val person: String = "",
    val amount: String = "",
    val accountId: String? = null,
    val accounts: List<ChoiceOption<String>> = emptyList(),
    val accountName: String? = null,
    /** Currency code of the chosen account, shown after the amount. */
    val currency: String? = null,
    val dueMillisUtc: Long? = null,
    val dueText: String? = null,
    val note: String = "",
    val personError: Boolean = false,
    val amountError: Boolean = false,
    val accountError: Boolean = false,
    val isDone: Boolean = false,
)
