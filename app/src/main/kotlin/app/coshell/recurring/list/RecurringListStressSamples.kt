package app.coshell.recurring.list

import app.coshell.core.designsystem.category.CategoryPalette
import app.coshell.core.domain.recurring.PaymentState
import app.coshell.core.model.Recurrence
import app.coshell.core.model.TransactionType
import app.coshell.templates.TemplateItem
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month
import kotlinx.datetime.YearMonth

/**
 * Stress sample of «Регулярные платежи» (#378): names of 50+ characters and a word without spaces, billion-sized
 * amounts, a long account name, overdue and automatic rows, a huge total of the month, 30+ rules and a long paused one.
 */
internal val recurringListStressSample: RecurringListUiState = run {
    val nov = YearMonth(2026, Month.NOVEMBER)
    val long = "Коммунальные платежи и обслуживание квартиры в Буэнос-Айресе"
    val word = "Подпискаонлайнкинотеатрабезпробеловвообще"
    fun row(
        n: Int, name: String, amount: String, currency: String, account: String, recurrence: Recurrence, day: Int,
        state: PaymentState = PaymentState.SOON, auto: Boolean = false, type: TransactionType = TransactionType.EXPENSE,
    ) = RecurringRow(
        TemplateItem("r$n", name, type, account, categoryName = "Дом", categoryIcon = "home",
            categoryColor = CategoryPalette.colors[n % CategoryPalette.colors.size], amount = if (amount.isEmpty()) "" else "$amount $currency", currency = currency),
        recurrence, auto, LocalDate(2026, 11, day), state, missed = if (state == PaymentState.MISSED) 12 else 0,
    )
    val account = "Тинькофф Накопительный Анастасии для отпуска в Патагонии"
    val head = listOf(
        row(1, long, "999 999 999,99", "USDT", account, Recurrence.Monthly(15), 13, PaymentState.MISSED),
        row(2, word, "", "ARS", "Galicia", Recurrence.Weekly(DayOfWeek.TUESDAY), 15, PaymentState.TODAY, auto = true),
        row(3, "Зарплата", "12 345 678", "JPY", "Токио", Recurrence.Monthly(16), 16, type = TransactionType.INCOME),
    )
    val tail = (4..33).map { row(it, "Платёж №$it", "${it} 000", "ARS", "Galicia", Recurrence.Monthly(it - 3), 10 + it % 20, auto = it % 3 == 0) }
    RecurringListUiState(
        total = 33,
        counts = mapOf(PaymentFilter.ALL to 33, PaymentFilter.EXPENSE to 32, PaymentFilter.INCOME to 1, PaymentFilter.AUTO to 11),
        ahead = MonthAheadUi(nov, count = 33, total = "−999 999 999 USDT", withoutAmount = 12, missing = listOf("KWD", "BTC", "CLP")),
        months = listOf(RecurringMonth(nov, (head + tail).sortedBy { it.date })),
        paused = listOf(PausedUi("p", long, account)),
    )
}
