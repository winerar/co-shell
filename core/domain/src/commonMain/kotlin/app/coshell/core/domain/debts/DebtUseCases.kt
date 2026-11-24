package app.coshell.core.domain.debts

import app.coshell.core.domain.AccountRepository
import app.coshell.core.model.DebtDirection
import app.coshell.core.model.DebtEvent
import app.coshell.core.model.DebtEventKind
import app.coshell.core.model.IdGenerator
import app.coshell.core.model.Money
import kotlin.time.Instant
import kotlinx.datetime.LocalDate

/** Why a debt, a return or a closing was refused (#552): what the screen tells the person to fix. */
enum class DebtProblem { PERSON, AMOUNT, ACCOUNT, CURRENCY, TOO_MUCH, NOT_FOUND, CLOSED }

sealed interface DebtResult {
    data class Saved(val debtId: String) : DebtResult
    data class Invalid(val problem: DebtProblem) : DebtResult
}

/**
 * «Одолжил я» / «Занял я» (#552): the money moves [accountId] — out of it for a loan, into it for a borrowing — and nothing is spent or
 * earned. The account must be live and in the debt's currency.
 */
class IssueDebtUseCase(private val debts: DebtRepository, private val accounts: AccountRepository, private val ids: IdGenerator) {
    suspend operator fun invoke(
        direction: DebtDirection,
        person: String,
        amount: Money,
        accountId: String,
        date: Instant,
        dueDate: LocalDate? = null,
        note: String = "",
    ): DebtResult {
        if (person.isBlank()) return DebtResult.Invalid(DebtProblem.PERSON)
        if (!amount.isPositive) return DebtResult.Invalid(DebtProblem.AMOUNT)
        accountProblem(accounts, accountId, amount.currency)?.let { return DebtResult.Invalid(it) }
        val debtId = ids.newId()
        debts.add(listOf(DebtEvent(ids.newId(), debtId, DebtEventKind.ISSUE, direction, amount, date, accountId, person.trim(), dueDate, note.trim())))
        return DebtResult.Saved(debtId)
    }
}

/** «Вернули часть» (#552): a return of at most what is left; it moves the account the other way than the issue did. */
class ReturnDebtUseCase(private val debts: DebtRepository, private val accounts: AccountRepository, private val ids: IdGenerator) {
    suspend operator fun invoke(debtId: String, amount: Money, accountId: String, date: Instant): DebtResult {
        val debt = DebtBook.of(debts.events()).firstOrNull { it.id == debtId } ?: return DebtResult.Invalid(DebtProblem.NOT_FOUND)
        if (debt.closed) return DebtResult.Invalid(DebtProblem.CLOSED)
        if (!amount.isPositive) return DebtResult.Invalid(DebtProblem.AMOUNT)
        accountProblem(accounts, accountId, debt.total.currency)?.let { return DebtResult.Invalid(it) }
        if (amount.currency != debt.total.currency) return DebtResult.Invalid(DebtProblem.CURRENCY)
        if (amount.amountMinor > debt.left.amountMinor) return DebtResult.Invalid(DebtProblem.TOO_MUCH)
        debts.add(listOf(DebtEvent(ids.newId(), debtId, DebtEventKind.RETURN, debt.direction, amount, date, accountId)))
        return DebtResult.Saved(debtId)
    }
}

/** «Закрыть долг» (#552): what is left is forgiven or settled outside the app; no account moves. */
class CloseDebtUseCase(private val debts: DebtRepository, private val ids: IdGenerator) {
    suspend operator fun invoke(debtId: String, date: Instant): DebtResult {
        val debt = DebtBook.of(debts.events()).firstOrNull { it.id == debtId } ?: return DebtResult.Invalid(DebtProblem.NOT_FOUND)
        if (debt.closed) return DebtResult.Invalid(DebtProblem.CLOSED)
        debts.add(listOf(DebtEvent(ids.newId(), debtId, DebtEventKind.CLOSE, debt.direction, Money(0, debt.total.currency), date)))
        return DebtResult.Saved(debtId)
    }
}

private suspend fun accountProblem(accounts: AccountRepository, accountId: String, currency: app.coshell.core.model.CurrencyCode): DebtProblem? {
    val account = accounts.getAccount(accountId)
    return when {
        account == null || account.archived -> DebtProblem.ACCOUNT
        account.currency != currency -> DebtProblem.CURRENCY
        else -> null
    }
}
