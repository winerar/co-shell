package app.coshell.recurring.list

import app.coshell.core.domain.recurring.PaymentState
import app.coshell.core.domain.recurring.ScheduledPayment
import app.coshell.core.model.PostMode
import app.coshell.core.model.Recurrence
import app.coshell.core.model.TransactionType
import app.coshell.templates.TemplateItem
import kotlinx.datetime.LocalDate
import kotlinx.datetime.YearMonth

/** Chips of «Регулярные платежи»: «Все · Расходы · Доходы · Переводы · Само». */
enum class PaymentFilter { ALL, EXPENSE, INCOME, TRANSFER, AUTO }

/**
 * A row: the template, its rule («Каждый месяц, 15-го»), the next payment and how it stands.
 * [missed] — overdue payments («пропущено 2»), [auto] — «Проводить само».
 */
data class RecurringRow(
    val item: TemplateItem,
    val recurrence: Recurrence,
    val auto: Boolean,
    val date: LocalDate,
    val state: PaymentState,
    val missed: Int = 0,
)

/** Rows whose next payment falls in [month], by date. */
data class RecurringMonth(val month: YearMonth, val rows: List<RecurringRow>)

/**
 * The hero «До конца ноября · 9 платежей»: [total] — the net of the payments with an amount in the main report
 * currency at today's rates («−410 USD», sign included), `null` when none has an amount; [withoutAmount] — those
 * entered when posting; [missing] — currencies without a rate, left out of [total].
 */
data class MonthAheadUi(
    val month: YearMonth,
    val count: Int,
    val total: String? = null,
    val withoutAmount: Int = 0,
    val missing: List<String> = emptyList(),
)

/**
 * «Счёт «Наличные» в архиве — платёж «Аренда» на паузе» (#406, board `Rec143Edge`); a tap opens the template to choose
 * another account. [accountName] `null` — the account was deleted.
 */
data class PausedUi(val templateId: String, val name: String, val accountName: String?)

data class RecurringListUiState(
    val isLoading: Boolean = false,
    val query: String = "",
    val filter: PaymentFilter = PaymentFilter.ALL,
    /** Rules in all — «Найти платёж · 24», «Все · 24». */
    val total: Int = 0,
    /** Rules per chip; chips with no rules are absent (except «Все»). */
    val counts: Map<PaymentFilter, Int> = emptyMap(),
    val ahead: MonthAheadUi? = null,
    val months: List<RecurringMonth> = emptyList(),
    /** Payments on pause (account archived), above the hero. */
    val paused: List<PausedUi> = emptyList(),
) {
    val isEmpty: Boolean get() = !isLoading && total == 0
    val nothingFound: Boolean get() = !isLoading && total > 0 && months.isEmpty()
}

/** The search box shows from this many rules on (fewer fit on one screen anyway). */
internal const val SEARCH_FROM = 9

/**
 * Pure: what «Регулярные платежи» shows. [payments] — every rule ([app.coshell.core.domain.recurring.scheduledPayments]),
 * [items] — the same templates ready to draw, by id.
 */
internal fun recurringList(
    payments: List<ScheduledPayment>,
    items: Map<String, TemplateItem>,
    ahead: MonthAheadUi?,
    query: String,
    filter: PaymentFilter,
    paused: List<PausedUi> = emptyList(),
): RecurringListUiState {
    val rows = payments.mapNotNull { payment -> items[payment.template.id]?.let { payment.toRow(it) } }
    val counts = PaymentFilter.entries.associateWith { chip -> rows.count { it.matches(chip) } }.filter { (chip, n) -> chip == PaymentFilter.ALL || n > 0 }
    // A chip whose last rule is gone would leave an empty screen with nothing to tap.
    val chip = filter.takeIf { it in counts } ?: PaymentFilter.ALL
    val needle = query.trim()
    val shown = rows.filter { it.matches(chip) && it.matches(needle) }
    return RecurringListUiState(
        query = query,
        filter = chip,
        total = rows.size,
        counts = counts,
        ahead = ahead,
        months = shown.groupBy { YearMonth(it.date.year, it.date.month) }.map { (month, list) -> RecurringMonth(month, list) },
        paused = paused,
    )
}

private fun ScheduledPayment.toRow(item: TemplateItem): RecurringRow? {
    val schedule = template.schedule ?: return null
    return RecurringRow(item, schedule.recurrence, schedule.mode == PostMode.AUTO, date, state, missed)
}

private fun RecurringRow.matches(chip: PaymentFilter): Boolean = when (chip) {
    PaymentFilter.ALL -> true
    PaymentFilter.EXPENSE -> item.type == TransactionType.EXPENSE
    PaymentFilter.INCOME -> item.type == TransactionType.INCOME
    PaymentFilter.TRANSFER -> item.type == TransactionType.TRANSFER
    PaymentFilter.AUTO -> auto
}

/** By name, category or account — «свет», «подписки», «galicia». */
private fun RecurringRow.matches(needle: String): Boolean = needle.isEmpty() ||
    listOf(item.name, item.categoryName, item.accountName, item.toAccountName).any { it.contains(needle, ignoreCase = true) }
