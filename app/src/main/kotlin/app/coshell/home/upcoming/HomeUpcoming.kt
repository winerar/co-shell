package app.coshell.home.upcoming

import app.coshell.core.domain.recurring.PaymentState
import app.coshell.core.domain.recurring.upcomingPayments
import app.coshell.core.model.Account
import app.coshell.core.model.Category
import app.coshell.core.model.MoneyFormat
import app.coshell.core.model.PostMode
import app.coshell.core.model.TransactionTemplate
import app.coshell.templates.TemplateItem
import app.coshell.templates.toItem
import kotlinx.datetime.LocalDate

/**
 * One row of «Скоро · 7 дней» on «Обзоре» (#143, board `Rec143HomeA`).
 * @property date the oldest payment not handled yet — «Провести» opens the sheet on this day.
 * @property missed overdue payments (1 — «пропущен», 2+ — «пропущено 2»); 0 for today and later.
 * @property auto «Проводить само» and not overdue: «само» instead of «Провести».
 */
data class UpcomingRow(
    val item: TemplateItem,
    val date: LocalDate,
    val state: PaymentState,
    val missed: Int = 0,
    val auto: Boolean = false,
)

/** Regular payments of the next days for «Обзор»: missed first, then by date ([upcomingPayments]). */
fun buildUpcomingRows(
    templates: List<TransactionTemplate>,
    accounts: Map<String, Account>,
    categories: Map<String, Category>,
    moneyFormat: MoneyFormat,
    today: LocalDate,
): List<UpcomingRow> = upcomingPayments(templates, today).map { payment ->
    val template = payment.template
    UpcomingRow(
        item = template.toItem(accounts, categories, moneyFormat),
        date = payment.date,
        state = payment.state,
        missed = payment.missed.size,
        // A missed «само» payment could not post itself (account archived, category gone): by hand then.
        auto = template.schedule?.mode == PostMode.AUTO && payment.state != PaymentState.MISSED,
    )
}
