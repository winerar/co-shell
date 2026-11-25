package app.coshell.budgets.notify

import android.app.Application
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.test.core.app.ApplicationProvider
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import androidx.work.testing.TestListenableWorkerBuilder
import app.coshell.budgets.BudgetSource
import app.coshell.core.domain.budgets.BudgetRepository
import app.coshell.core.domain.reports.ReportDay
import app.coshell.core.domain.settings.MonthStartSettings
import app.coshell.core.domain.settings.ReminderSettings
import app.coshell.core.model.Budget
import app.coshell.core.model.Category
import app.coshell.core.model.CategoryType
import app.coshell.core.model.CurrencyCode
import app.coshell.core.model.Money
import app.coshell.core.model.MoneyFormat
import app.coshell.core.model.TransactionType
import app.coshell.testing.FakeCategoryRepository
import app.coshell.testing.FakeExchangeRateRepository
import app.coshell.testing.FakeReportCurrencySettings
import app.coshell.testing.FakeReportRepository
import app.coshell.testing.FakeTransactionRepository
import kotlin.time.Clock
import kotlin.time.Instant
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.YearMonth
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

/** The daily limit check (#186, board `Not186B`): one notification a day, once per limit and kind, silent when switched off. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "ru", application = Application::class)
class LimitAlertsWorkerTest {
    private val context: Context = ApplicationProvider.getApplicationContext()
    private val usd = CurrencyCode("USD")
    private val tz = TimeZone.UTC
    private val manager = context.getSystemService(NotificationManager::class.java)

    private val categories = FakeCategoryRepository()
    private val reports = FakeReportRepository()
    private val budgets = object : BudgetRepository {
        override fun observeBudgets() = MutableStateFlow(listOf(Budget("food:2026-11", "food", YearMonth(2026, 11), Money(1_000_00, usd))))
        override suspend fun getBudgets() = observeBudgets().value
        override suspend fun applyBudgets(save: List<Budget>, deleteIds: List<String>) = Unit
    }
    private val monthStart = object : MonthStartSettings {
        override val monthStartDay = MutableStateFlow(1)
        override suspend fun setMonthStartDay(day: Int) = Unit
    }
    private val settings = object : ReminderSettings {
        override val enabled = MutableStateFlow(true)
        override suspend fun setEnabled(enabled: Boolean) = Unit
        override val hour = MutableStateFlow(9)
        override suspend fun setHour(hour: Int) = Unit
        override val daysBefore = MutableStateFlow(0)
        override suspend fun setDaysBefore(days: Int) = Unit
        override val limitsEnabled = MutableStateFlow(true)
        override suspend fun setLimitsEnabled(enabled: Boolean) { limitsEnabled.value = enabled }
    }
    private var now = Instant.parse("2026-11-20T09:00:00Z")
    private val clock = object : Clock {
        override fun now() = now
    }
    private val source = BudgetSource(
        budgets, categories, FakeTransactionRepository(), reports, FakeExchangeRateRepository(), FakeReportCurrencySettings(), monthStart, tz,
    )

    private fun run() = runTest {
        val worker = TestListenableWorkerBuilder<LimitAlertsWorker>(context).setWorkerFactory(object : WorkerFactory() {
            override fun createWorker(appContext: Context, workerClassName: String, workerParameters: WorkerParameters) =
                LimitAlertsWorker(appContext, workerParameters, source, settings, LimitAlertLog(appContext), LimitNotices(appContext, MoneyFormat.RU), clock, tz)
        }).build()
        assertEquals(ListenableWorker.Result.success(), worker.doWork())
    }

    private fun shown() = shadowOf(manager).allNotifications

    private fun spend(units: Long) {
        reports.days.value = listOf(ReportDay(LocalDate(2026, 11, 5), "food", TransactionType.EXPENSE, Money(units * 100, usd)))
    }

    @Before
    fun setUp() = runTest {
        shadowOf(context as Application).grantPermissions(android.Manifest.permission.POST_NOTIFICATIONS)
        categories.saveCategory(Category("food", "Продукты", CategoryType.EXPENSE))
    }

    @Test
    fun `eighty percent of a limit says what is still available`() {
        spend(850)
        run()

        val n = shown().single()
        assertEquals("Продукты: ещё можно 150 USD", n.extras.getString(NotificationCompat.EXTRA_TITLE))
        assertEquals("Это 15 % лимита на 11 дней. Лимит — ориентир, а не запрет.", n.extras.getCharSequence(NotificationCompat.EXTRA_TEXT).toString())
        assertEquals(LimitNotices.CHANNEL, n.channelId)
    }

    @Test
    fun `once a day and once per limit`() {
        spend(850)
        run()
        manager.cancelAll()
        run()
        assertTrue(shown().isEmpty())

        now = Instant.parse("2026-11-21T09:00:00Z")
        run()
        assertTrue("the same limit and kind was told this month", shown().isEmpty())
    }

    @Test
    fun `nothing when switched off or calm`() {
        settings.limitsEnabled.value = false
        spend(850)
        run()
        assertTrue(shown().isEmpty())

        settings.limitsEnabled.value = true
        spend(100)
        run()
        assertTrue(shown().isEmpty())
    }
}
