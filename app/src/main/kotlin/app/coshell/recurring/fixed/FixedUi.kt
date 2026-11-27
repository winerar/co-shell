package app.coshell.recurring.fixed

import app.coshell.core.domain.rates.CurrencyConverter
import app.coshell.core.domain.rates.RateBook
import app.coshell.core.domain.recurring.FixedSpending
import app.coshell.core.domain.recurring.activePayments
import app.coshell.core.domain.recurring.fixedSpending
import app.coshell.core.domain.reports.MonthTotals
import app.coshell.core.model.Account
import app.coshell.core.model.Category
import app.coshell.core.model.CurrencyCode
import app.coshell.core.model.MoneyFormat
import app.coshell.core.model.Recurrence
import app.coshell.core.model.TransactionTemplate
import app.coshell.templates.TemplateItem
import app.coshell.templates.toItem
import kotlinx.datetime.LocalDate

/**
 * «Постоянные траты» ready to draw (#771): the same block in «Отчёты» (card) and «Регулярные → За год» (hero + list).
 * [yearly]/[monthly] — the sum in the totals currency, [count] — payments in it, [sharePercent] — of the spending of the last
 * months (`null` — nothing to compare with), [missing] — currencies without a rate, left out.
 */
data class FixedUi(
    val yearly: String,
    val monthly: String,
    val count: Int,
    val sharePercent: Int?,
    val missing: List<String>,
    val rows: List<FixedRowUi>,
)

/** A payment: the template (badge, name), its rule, a year of it in its own currency and a month as a bare number («650»). */
data class FixedRowUi(val item: TemplateItem, val recurrence: Recurrence, val yearly: String, val monthly: String)

/** Pure: [fixed] with the templates already drawn as [items] (by id). */
internal fun fixedUi(fixed: FixedSpending, items: Map<String, TemplateItem>, format: MoneyFormat): FixedUi = FixedUi(
    yearly = format.format(fixed.yearly),
    monthly = format.format(fixed.monthly),
    count = fixed.items.size,
    sharePercent = fixed.sharePercent,
    missing = fixed.missing.map { it.value }.sorted(),
    rows = fixed.items.mapNotNull { f ->
        val item = items[f.template.id] ?: return@mapNotNull null
        val recurrence = f.template.schedule?.recurrence ?: return@mapNotNull null
        FixedRowUi(item, recurrence, format.format(f.yearly), format.format(f.monthly).removeSuffix(MoneyFormat.NBSP + f.monthly.currency.value))
    },
)

/**
 * Pure: «Постоянные траты» from every template — the active ones (an archived account pauses a payment, #406) with «Повтор»
 * and an amount, in [currency] at the rates of [today]; [months] — spending by month in [currency] for the share. `null` — none.
 */
internal fun buildFixed(
    templates: List<TransactionTemplate>,
    accounts: Map<String, Account>,
    categories: Map<String, Category>,
    book: RateBook,
    currency: CurrencyCode,
    today: LocalDate,
    months: List<MonthTotals>,
    format: MoneyFormat,
): FixedUi? {
    val active = activePayments(templates.filter { it.schedule != null }, accounts)
    val fixed = fixedSpending(active, CurrencyConverter(book), currency, today, months) ?: return null
    return fixedUi(fixed, fixed.items.associate { it.template.id to it.template.toItem(accounts, categories, format) }, format)
}
