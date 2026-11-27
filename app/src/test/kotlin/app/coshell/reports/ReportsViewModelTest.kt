package app.coshell.reports

import app.coshell.core.model.Recurrence
import app.coshell.core.model.RecurringSchedule
import app.coshell.core.model.TransactionTemplate
import app.coshell.testing.FakeTemplateRepository
import app.coshell.core.domain.family.OtherMembers
import app.coshell.core.domain.reports.BalanceDay
import app.coshell.core.domain.reports.ReportDay
import app.coshell.core.domain.reports.ReportPeriod
import app.coshell.core.domain.reports.ReportPeriodKind
import app.coshell.core.domain.settings.InMemoryScreenOptions
import app.coshell.core.domain.settings.ReportsStart
import app.coshell.core.domain.settings.ScreenOptions
import app.coshell.core.domain.settings.MonthStartSettings
import app.coshell.core.model.Account
import app.coshell.core.model.Category
import app.coshell.core.model.CategoryType
import app.coshell.core.model.CurrencyCode
import app.coshell.core.model.Money
import app.coshell.core.model.MoneyFormat
import app.coshell.core.model.TransactionType
import app.coshell.reports.ReportsUiState.Comparison
import app.coshell.testing.FakeAccountRepository
import app.coshell.testing.FakeCategoryRepository
import app.coshell.testing.FakeExchangeRateRepository
import app.coshell.testing.FakeReportCurrencySettings
import app.coshell.testing.FakeReportRepository
import app.coshell.testing.FakeTagRepository
import app.coshell.testing.FakeTransactionRepository
import app.coshell.testing.MainDispatcherRule
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.time.Clock
import kotlin.time.Instant
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.YearMonth
import kotlinx.datetime.atStartOfDayIn
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test

class ReportsViewModelTest {
    @get:Rule val main = MainDispatcherRule()

    private val usd = CurrencyCode("USD")
    private fun usd(units: Long) = Money(units * 100, usd)

    private val monthStart = object : MonthStartSettings {
        override val monthStartDay = MutableStateFlow(MonthStartSettings.DEFAULT)
        override suspend fun setMonthStartDay(day: Int) { monthStartDay.value = day }
    }

    /** «Today» is 11 November 2026. */
    private val clock = object : Clock {
        override fun now(): Instant = LocalDate(2026, 11, 11).atStartOfDayIn(TimeZone.UTC)
    }

    private val categories = FakeCategoryRepository().apply {
        items.value = listOf(
            Category("home", "Дом", CategoryType.EXPENSE),
            Category("rent", "Аренда", CategoryType.EXPENSE, parentId = "home"),
            Category("cafe", "Кафе", CategoryType.EXPENSE),
            Category("salary", "Зарплата", CategoryType.INCOME),
        ).associateBy { it.id }
    }

    private fun day(month: Int, d: Int, category: String, units: Long, type: TransactionType = TransactionType.EXPENSE) =
        ReportDay(LocalDate(2026, month, d), category, type, usd(units))

    private val reports = FakeReportRepository(
        listOf(
            day(10, 5, "rent", 500),
            day(10, 20, "cafe", 100),
            day(11, 2, "rent", 300),
            day(11, 3, "home", 40),
            day(11, 10, "cafe", 60),
            day(11, 4, "salary", 2_000, TransactionType.INCOME),
            day(6, 15, "cafe", 10),
        ),
    )

    private val others = MutableStateFlow(emptyMap<String, String>())

    private val templates = FakeTemplateRepository()

    private val accounts = FakeAccountRepository().apply {
        items.value = mapOf("cash" to Account("cash", "Доллары", usd, initialBalanceMinor = 1_000_00))
    }

    private fun viewModel(options: ScreenOptions = ScreenOptions(), transactions: FakeTransactionRepository = FakeTransactionRepository()) = ReportsViewModel(
        reports,
        categories,
        accounts,
        FakeExchangeRateRepository(),
        OtherMembers { others },
        transactions,
        FakeTagRepository(),
        templates,
        FakeReportCurrencySettings(listOf(usd, CurrencyCode("ARS"))),
        monthStart,
        MoneyFormat.RU,
        clock,
        TimeZone.UTC,
        InMemoryScreenOptions(options),
    ).apply { work = EmptyCoroutineContext }

    @Test
    fun `repeating expenses make the fixed card with a year, a month and their share`() = runTest {
        templates.items.value = mapOf(
            "gym" to TransactionTemplate(
                "gym", "Спортзал", TransactionType.EXPENSE, "cash", usd, usd(30), categoryId = "home",
                schedule = RecurringSchedule(Recurrence.Monthly(5), LocalDate(2026, 1, 5)),
            ),
            "coffee" to TransactionTemplate("coffee", "Кофе", TransactionType.EXPENSE, "cash", usd, usd(3)),
        )
        val vm = viewModel()
        vm.state.launchIn(backgroundScope)
        val fixed = vm.state.first { it.fixed != null }.fixed!!
        assertEquals("360${MoneyFormat.NBSP}USD", fixed.yearly)
        assertEquals("30${MoneyFormat.NBSP}USD", fixed.monthly)
        assertEquals(listOf("Спортзал"), fixed.rows.map { it.item.name })
        // June (10) and October (600) are the full months with spending: 30 of 305 a month.
        assertEquals(10, fixed.sharePercent)
    }

    @Test
    fun `opens on the current month with subcategories in their parent and the month before as grey marks`() = runTest {
        val vm = viewModel()
        vm.state.launchIn(backgroundScope)
        val state = vm.state.first { it.spending != null }

        assertEquals(ReportPeriod.month(YearMonth(2026, 11)), state.period)
        assertEquals("USD", state.currency)
        val spending = state.spending!!
        assertEquals("400", spending.total)
        assertEquals(listOf("home" to 85, "cafe" to 15), spending.bars.map { it.categoryId to it.percent })
        assertEquals(listOf("500", "100"), spending.bars.map { it.ghostValue })
        // October's 500 for «Дом» is the largest mark: the whole scale.
        assertEquals(1f, spending.bars[0].ghost!!, 0.001f)
        assertEquals(listOf("rent", "cafe", "home"), spending.categories.map { it.categoryId })
        val comparison = spending.comparison as Comparison.SoFar
        assertEquals(11, comparison.days)
        assertEquals("500", comparison.previous)
    }

    @Test
    fun `opens on the year the settings ask for, and a pick of the user wins over them`() = runTest {
        val vm = viewModel(ScreenOptions(reportsStart = ReportsStart.YEAR, reportsCompare = false))
        vm.state.launchIn(backgroundScope)
        val state = vm.state.first { it.spending != null }

        assertEquals(ReportPeriod.year(2026), state.period)
        assertEquals(false, state.compare)

        vm.onMonth(YearMonth(2026, 10))
        assertEquals(ReportPeriod.month(YearMonth(2026, 10)), vm.state.first { it.period?.kind == ReportPeriodKind.MONTH }.period)
    }

    @Test
    fun `opens on the quarter the settings ask for`() = runTest {
        val vm = viewModel(ScreenOptions(reportsStart = ReportsStart.QUARTER))
        vm.state.launchIn(backgroundScope)
        assertEquals(ReportPeriod.quarter(YearMonth(2026, 11)), vm.state.first { it.spending != null }.period)
    }

    @Test
    fun `columns are six months up to the shown one, it is the highlighted last`() = runTest {
        val vm = viewModel()
        vm.state.launchIn(backgroundScope)
        val months = vm.state.first { it.months != null }.months!!

        assertEquals((6..11).map { YearMonth(2026, it) }, months.columns.map { it.month })
        assertEquals(listOf("10", "0", "0", "0", "600", "400"), months.columns.map { it.value })
        assertEquals(5, months.selected)
    }

    @Test
    fun `arrows step back a month, a finished month is compared whole and empty months have no spending`() = runTest {
        val vm = viewModel()
        vm.state.launchIn(backgroundScope)
        vm.state.first { it.spending != null }

        vm.onShift(-1)
        val october = vm.state.first { it.period == ReportPeriod.month(YearMonth(2026, 10)) }
        assertEquals("600", october.spending!!.total)
        assertEquals(Comparison.Whole(ReportPeriodKind.MONTH, "0", more = true), october.spending!!.comparison)

        vm.onShift(-1)
        assertNull(vm.state.first { it.period == ReportPeriod.month(YearMonth(2026, 9)) }.spending)
    }

    @Test
    fun `the sheet applies a quarter without comparison and another currency`() = runTest {
        val vm = viewModel()
        vm.state.launchIn(backgroundScope)
        vm.state.first { it.spending != null }

        vm.onApply(ReportPeriod.quarter(YearMonth(2026, 11)), "USD", compare = false)
        val quarter = vm.state.first { it.period?.kind == ReportPeriodKind.QUARTER }
        assertEquals("1\u202F000", quarter.spending!!.total)
        assertNull(quarter.spending!!.comparison)
        assertNull(quarter.spending!!.bars.first().ghost)

        vm.onCurrency("ARS")
        val ars = vm.state.first { it.currency == "ARS" }
        // No USD → ARS rate: nothing counted, the currency is named as left out.
        assertNull(ars.spending)
    }

    @Test
    fun `a month column opens that month`() = runTest {
        val vm = viewModel()
        vm.state.launchIn(backgroundScope)
        vm.state.first { it.spending != null }

        vm.onMonth(YearMonth(2026, 6))

        assertEquals("10", vm.state.first { it.period?.firstMonth == YearMonth(2026, 6) }.spending!!.total)
    }

    @Test
    fun `capital is a year of month ends with why the last months changed, members split the spending`() = runTest {
        reports.balanceDays.value = listOf(BalanceDay(LocalDate(2026, 10, 5), "cash", -600_00), BalanceDay(LocalDate(2026, 11, 4), "cash", 1_600_00))
        others.value = mapOf("anya" to "Аня")
        val vm = viewModel()
        vm.state.launchIn(backgroundScope)
        val state = vm.state.first { it.capital != null }

        val capital = state.capital!!
        assertEquals(12, capital.points.size)
        assertEquals(listOf("1\u202F000", "400", "2\u202F000"), capital.points.takeLast(3).map { it.value })
        assertEquals("+1\u202F000", capital.change)
        // November: saved 2 000 − 400 = 1 600, all of the change; October: −600 spent.
        assertEquals(listOf("+1\u202F600", "-600"), capital.why.take(2).map { it.saved.replace('\u2212', '-') })
        assertEquals("0", capital.why.first().rates)
        assertEquals(listOf<String?>(null), state.members.map { it.name })
    }

    @Test
    fun `calendar is the shown month in weeks starting on Monday, brighter days for bigger spending`() = runTest {
        val vm = viewModel()
        vm.state.launchIn(backgroundScope)
        val calendar = vm.state.first { it.calendar != null }.calendar!!

        // 1 November 2026 is a Sunday: six empty places in the Monday-first week.
        assertEquals(6, calendar.leadingBlanks)
        assertEquals(30, calendar.cells.size)
        assertEquals(listOf(2, 3, 10), calendar.cells.filter { it.spent != null }.map { it.date.day })
        assertEquals(listOf(4, 1, 2), calendar.cells.filter { it.spent != null }.map { it.level })
        assertEquals(true, calendar.cells[10].today)
        assertEquals(true, calendar.cells[11].future)
        // The 1st..11th is 11 days, three with spending.
        assertEquals(8, calendar.noSpendDays)
    }

    @Test
    fun `a tapped day shows its operations under the grid and the same day again hides them`() = runTest {
        val transactions = FakeTransactionRepository()
        transactions.saveTransaction(
            app.coshell.core.model.Transaction("t1", TransactionType.EXPENSE, "cash", usd(60), LocalDate(2026, 11, 10).atStartOfDayIn(TimeZone.UTC), categoryId = "cafe"),
        )
        val vm = viewModel(transactions = transactions)
        vm.state.launchIn(backgroundScope)
        vm.state.first { it.calendar != null }

        vm.onDay(LocalDate(2026, 11, 10))
        val shown = vm.state.first { it.dayOps != null }
        assertEquals(LocalDate(2026, 11, 10), shown.selectedDay)
        assertEquals(listOf("t1"), shown.dayOps!!.rows.map { it.id })

        vm.onDay(LocalDate(2026, 11, 10))
        val hidden = vm.state.first { it.selectedDay == null }
        assertNull(hidden.dayOps)
    }

    @Test
    fun `a day of another month is dropped when the period moves`() = runTest {
        val vm = viewModel()
        vm.state.launchIn(backgroundScope)
        vm.state.first { it.calendar != null }
        vm.onDay(LocalDate(2026, 11, 10))
        vm.state.first { it.selectedDay != null }

        vm.onShift(-1)

        assertNull(vm.state.first { it.period == ReportPeriod.month(YearMonth(2026, 10)) }.selectedDay)
    }
}
