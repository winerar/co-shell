package app.coshell.core.domain.recurring

import app.coshell.core.model.Account
import app.coshell.core.model.TransactionTemplate

/**
 * A regular payment that cannot go on (#406, board `Rec143Edge`): its account — or, for a transfer, the account
 * it goes to — is archived or deleted. Such a payment is «на паузе»: not in «Скоро», not reminded, «само» does
 * not post, the month total leaves it out. Its days still pile up as missed («копятся, не теряются»): once another
 * account is chosen in the template, they can be posted or skipped. [accountName] — the archived account
 * («Счёт «Наличные» в архиве»); `null` — the account is gone altogether.
 */
data class PausedPayment(val template: TransactionTemplate, val accountName: String?)

/**
 * Why [template] is paused, or `null` when it is not (or has no «Повтор»). [accounts] — every account by id,
 * archived ones included: a missing id means a deleted account.
 */
fun pausedPayment(template: TransactionTemplate, accounts: Map<String, Account>): PausedPayment? {
    if (template.schedule == null) return null
    val ids = listOfNotNull(template.accountId, template.toAccountId)
    for (id in ids) {
        val account = accounts[id]
        if (account == null || account.archived) return PausedPayment(template, account?.name)
    }
    return null
}

/** Paused payments among [templates], by name — the messages on «Регулярные платежи». */
fun pausedPayments(templates: List<TransactionTemplate>, accounts: Map<String, Account>): List<PausedPayment> =
    templates.mapNotNull { pausedPayment(it, accounts) }.sortedBy { it.template.name.lowercase() }

/** [templates] without the paused ones: what «Скоро», the reminder and the month total work with. */
fun activePayments(templates: List<TransactionTemplate>, accounts: Map<String, Account>): List<TransactionTemplate> =
    templates.filter { pausedPayment(it, accounts) == null }
