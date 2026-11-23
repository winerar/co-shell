package app.coshell.debts.edit

import androidx.compose.runtime.Immutable
import app.coshell.ui.form.ChoiceOption

/** One return line: «12 янв. · 300 USD». */
@Immutable
data class DebtReturnUi(val id: String, val text: String)

/** A debt's card (#552). [found] false — the debt is gone (deleted on the other phone): the sheet closes. */
@Immutable
data class DebtDetailUiState(
    val found: Boolean = true,
    val loaded: Boolean = false,
    val lent: Boolean = true,
    val person: String = "",
    val left: String = "",
    val total: String = "",
    val returned: String = "",
    val fraction: Float = 0f,
    val dueText: String? = null,
    val note: String = "",
    val closed: Boolean = false,
    val returns: List<DebtReturnUi> = emptyList(),
    val currency: String = "",
    val accounts: List<ChoiceOption<String>> = emptyList(),
    val returnAccountId: String? = null,
    val returnAccountName: String? = null,
    val returnAmount: String = "",
    val returnError: Int? = null,
    val returnDone: Boolean = false,
    val isDone: Boolean = false,
)
