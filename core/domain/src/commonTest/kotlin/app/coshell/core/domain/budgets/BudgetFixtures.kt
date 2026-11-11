package app.coshell.core.domain.budgets

import app.coshell.core.model.Category
import app.coshell.core.model.CategoryType
import app.coshell.core.model.CurrencyCode
import app.coshell.core.model.ExchangeRate
import app.coshell.core.model.Money
import app.coshell.core.model.Rate
import app.coshell.core.model.RateOrigin
import app.coshell.core.model.Transaction
import app.coshell.core.model.TransactionType
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.YearMonth
import kotlinx.datetime.toInstant

/** Shared data of the budget calculation tests: folder «Дом» with two categories, «Кафе», ARS at 1000 then 2000 per USD. */
internal object BudgetFixtures {
    val ars = CurrencyCode("ARS")
    val usd = CurrencyCode("USD")
    val amd = CurrencyCode("AMD")
    val tz = TimeZone.of("America/Argentina/Buenos_Aires")

    val september = YearMonth(2026, 9)
    val october = YearMonth(2026, 10)
    val november = YearMonth(2026, 11)
    val today: LocalDate = LocalDate(2026, 11, 11)

    val categories = listOf(
        Category("home", "Дом", CategoryType.EXPENSE),
        Category("rent", "Аренда", CategoryType.EXPENSE, parentId = "home"),
        Category("utilities", "Коммуналка", CategoryType.EXPENSE, parentId = "home", archived = true),
        Category("cafe", "Кафе", CategoryType.EXPENSE),
        Category("salary", "Зарплата", CategoryType.INCOME),
    )

    val rates = listOf(
        ExchangeRate(LocalDate(2026, 9, 1), usd, ars, Rate.parse("1000"), RateOrigin.DOWNLOADED),
        ExchangeRate(LocalDate(2026, 11, 5), usd, ars, Rate.parse("2000"), RateOrigin.DOWNLOADED),
    )

    fun ars(major: Long) = Money.ofMajor(major, currency = ars)
    fun usd(major: Long) = Money.ofMajor(major, currency = usd)

    private var nextId = 0

    fun operation(
        categoryId: String?,
        amount: Money,
        month: Int,
        day: Int,
        type: TransactionType = TransactionType.EXPENSE,
        hour: Int = 12,
    ) = Transaction(
        id = "t${nextId++}",
        type = type,
        accountId = "acc",
        amount = amount,
        date = LocalDateTime(2026, month, day, hour, 0).toInstant(tz),
        categoryId = categoryId,
        toAccountId = "other".takeIf { type == TransactionType.TRANSFER },
        toAmount = amount.takeIf { type == TransactionType.TRANSFER },
    )
}
