package app.coshell.core.domain.receipts

import app.coshell.core.model.Account

/**
 * Finds the account a bank receipt names by the last digits of its card or account number (#577).
 * Only a live account with exactly these digits counts: two accounts with the same digits are ambiguous — the sheet asks.
 */
object ReceiptAccounts {
    fun find(accounts: List<Account>, digits: String?): Account? {
        if (digits == null) return null
        return accounts.filter { !it.archived && it.cardDigits == digits }.singleOrNull()
    }
}
