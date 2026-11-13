package app.coshell.core.domain.quickentry

import app.coshell.core.model.Account
import app.coshell.core.model.CurrencyCode
import app.coshell.core.model.Money
import app.coshell.core.model.Transaction
import app.coshell.core.model.TransactionType

/**
 * Tips paid from another account (#322, owner 2026-11-13: «чаевые с другого счёта» — the bill by card, the tip in cash).
 *
 * A tip is a separate expense with the main operation's category, date, tags and payee, so budgets and reports see it
 * where the meal is. The percent is taken from what the receipt said ([Transaction.originalAmount] when paid in another
 * currency). A tip in a currency other than the tip account's is stored like a receipt in another currency:
 * the account's amount plus [Transaction.originalAmount].
 */
object Tips {

    /** Quick choices in the sheet; «Своя сумма» is typed on the keypad. */
    val PERCENTS = listOf(5, 10, 15)

    /** What the percent is taken from: the receipt amount when there is one. */
    fun base(main: Transaction): Money = main.originalAmount ?: main.amount

    /** [percent] of [base], rounded half to even to the minor unit. */
    fun percentOf(base: Money, percent: Int): Money {
        require(percent in 1..100) { "Tip percent must be 1..100, was $percent" }
        return base.abs().scale(percent.toLong(), 100)
    }

    sealed interface Result {
        data class Ok(val transaction: Transaction) : Result
        data class Invalid(val reason: Reason) : Result
    }

    enum class Reason { MAIN_NOT_EXPENSE, TIP_NOT_POSITIVE, NO_RATE }

    /**
     * The tip operation for [main], paid from [account]. [convert] turns the tip into the account's currency at the
     * operation date (`null` — no rate known). [note] comes from resources («Чаевые»); [id] from the id generator.
     */
    fun transaction(
        main: Transaction,
        tip: Money,
        account: Account,
        id: String,
        note: String,
        convert: (Money, CurrencyCode) -> Money?,
    ): Result {
        if (main.type != TransactionType.EXPENSE) return Result.Invalid(Reason.MAIN_NOT_EXPENSE)
        if (!tip.isPositive) return Result.Invalid(Reason.TIP_NOT_POSITIVE)
        val sameCurrency = tip.currency == account.currency
        val amount = if (sameCurrency) tip else convert(tip, account.currency) ?: return Result.Invalid(Reason.NO_RATE)
        if (!amount.isPositive) return Result.Invalid(Reason.TIP_NOT_POSITIVE)
        return Result.Ok(
            Transaction(
                id = id,
                type = TransactionType.EXPENSE,
                accountId = account.id,
                amount = amount,
                date = main.date,
                categoryId = main.categoryId,
                note = note,
                payee = main.payee,
                labels = main.labels,
                originalAmount = tip.takeUnless { sameCurrency },
                authorMemberId = main.authorMemberId,
            ),
        )
    }
}
