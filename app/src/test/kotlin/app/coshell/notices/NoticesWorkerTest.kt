package app.coshell.notices

import android.app.Application
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.test.core.app.ApplicationProvider
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import androidx.work.testing.TestListenableWorkerBuilder
import app.coshell.backup.auto.AutoBackupSettings
import app.coshell.budgets.BudgetSource
import app.coshell.core.domain.budgets.BudgetRepository
import app.coshell.core.domain.drafts.DraftRepository
import app.coshell.core.domain.notices.NoticeKind
import app.coshell.core.domain.notices.NoticeSettings
import app.coshell.core.domain.reports.ReportDay
import app.coshell.core.domain.settings.MonthStartSettings
import app.coshell.core.model.Account
import app.coshell.core.model.Budget
import app.coshell.core.model.Category
import app.coshell.core.model.CategoryType
import app.coshell.core.model.CurrencyCode
import app.coshell.core.model.Draft
import app.coshell.core.model.Money
import app.coshell.core.model.MoneyFormat
import app.coshell.core.model.Transaction
import app.coshell.core.model.TransactionType
import app.coshell.hints.FakeHintStore
import app.coshell.testing.FakeAccountRepository
import app.coshell.testing.FakeCategoryRepository
import app.coshell.testing.FakeExchangeRateRepository
import app.coshell.testing.FakeReportCurrencySettings
import app.coshell.testing.FakeReportRepository
import app.coshell.testing.FakeTransactionRepository
import kotlin.time.Clock
import kotlin.time.Instant
import kotlinx.coroutines.flow.Flow
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

/**
 * The daily check of the optional notifications (#745, #186): nothing until a kind is on, at most one a day, a case once; the limit one
 * speaks with its own words and opens its envelope (`LimitNotices`).
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "ru", application = Application::class)
class NoticesWorkerTest {
    private val context: Context = ApplicationProvider.getApplicationContext()
    private val usd = CurrencyCode("USD")
    private val tz = TimeZone.UTC
    private val manager = context.getSystemService(NotificationManager::class.java)

    private val categories = FakeCategoryRepository()
    private val accounts = FakeAccountRepository()
    private val transactions = FakeTransactionRepository()
    private val reports = FakeReportRepository()
    private val budgets = object : BudgetRepository {
        override fun observeBudgets() = MutableStateFlow(
            listOf(
                Budget("food:2026-11", "food", YearMonth(2026, 11), Money(1_000_00, usd)),
                Budget("cafe:2026-11", "cafe", YearMonth(2026, 11), Money(1_000_00, usd)),
            ),
        )
        override suspend fun getBudgets() = observeBudgets().value
        override suspend fun applyBudgets(save: List<Budget>, deleteIds: List<String>) = Unit
    }
    private val monthStart = object : MonthStartSettings {
        override val monthStartDay = MutableStateFlow(1)
        override suspend fun setMonthStartDay(day: Int) = Unit
    }
    private val drafts = object : DraftRepository {
        override fun observeDrafts(): Flow<List<Draft>> = MutableStateFlow(emptyList())
        override suspend fun getDraft(id: String): Draft? = null
        override suspend fun saveDrafts(drafts: List<Draft>) = Unit
        override suspend fun deleteDrafts(ids: List<String>) = Unit
    }
    private val settings = object : NoticeSettings {
        val kinds = MutableStateFlow<Set<NoticeKind>>(emptySet())
        override val enabled: Flow<Set<NoticeKind>> = kinds
        override suspend fun setEnabled(kind: NoticeKind, on: Boolean) { kinds.value = if (on) kinds.value + kind else kinds.value - kind }
        override val hour = MutableStateFlow(21)
        override suspend fun setHour(hour: Int) = Unit
        override val offerAnswered = MutableStateFlow(false)
        override suspend fun setOfferAnswered() { offerAnswered.value = true }
    }
    private var now = Instant.parse("2026-11-20T21:00:00Z")
    private val clock = object : Clock {
        override fun now() = now
    }
    private val budgetSource = BudgetSource(
        budgets, categories, transactions, reports, FakeExchangeRateRepository(), FakeReportCurrencySettings(), monthStart, tz,
    )

    private fun run() = runTest {
        val worker = TestListenableWorkerBuilder<NoticesWorker>(context).setWorkerFactory(object : WorkerFactory() {
            override fun createWorker(appContext: Context, workerClassName: String, workerParameters: WorkerParameters): ListenableWorker {
                val log = NoticeLog(appContext)
                val source = NoticeFactsSource(transactions, accounts, categories, drafts, budgetSource, FakeHintStore(), AutoBackupSettings(appContext), MoneyFormat.RU, log, clock, tz)
                return NoticesWorker(appContext, workerParameters, settings, log, source, NoticePosts(appContext))
            }
        }).build()
        assertEquals(ListenableWorker.Result.success(), worker.doWork())
    }

    private fun shown() = shadowOf(manager).allNotifications

    private fun spend(units: Long, cafe: Long = 0) {
        reports.days.value = listOfNotNull(
            ReportDay(LocalDate(2026, 11, 5), "food", TransactionType.EXPENSE, Money(units * 100, usd)),
            ReportDay(LocalDate(2026, 11, 5), "cafe", TransactionType.EXPENSE, Money(cafe * 100, usd)).takeIf { cafe > 0 },
        )
    }

    private fun title(n: android.app.Notification) = n.extras.getString(NotificationCompat.EXTRA_TITLE)

    @Before
    fun setUp() = runTest {
        shadowOf(context as Application).grantPermissions(android.Manifest.permission.POST_NOTIFICATIONS)
        categories.saveCategory(Category("food", "Продукты", CategoryType.EXPENSE))
        categories.saveCategory(Category("cafe", "Кафе", CategoryType.EXPENSE))
        accounts.saveAccount(Account("a", "Карта", usd))
        // The last entry was yesterday: nothing was written today.
        transactions.saveTransaction(Transaction("t1", TransactionType.EXPENSE, "a", Money(5_00, usd), Instant.parse("2026-11-19T12:00:00Z"), categoryId = "food"))
    }

    @Test
    fun `nothing is told until a kind is turned on`() {
        run()
        assertTrue(shown().isEmpty())
    }

    @Test
    fun `evening reminder asks when nothing was written today, once a day`() {
        settings.kinds.value = setOf(NoticeKind.EVENING)
        run()
        assertEquals("Как прошёл день?", shown().single().extras.getString(NotificationCompat.EXTRA_TITLE))
        manager.cancelAll()
        run()
        assertTrue("one a day", shown().isEmpty())

        now = Instant.parse("2026-11-21T21:00:00Z")
        run()
        assertEquals(1, shown().size)
    }

    @Test
    fun `an entry today keeps the evening reminder quiet`() {
        settings.kinds.value = setOf(NoticeKind.EVENING)
        transactions.items.value += "t2" to Transaction("t2", TransactionType.EXPENSE, "a", Money(3_00, usd), Instant.parse("2026-11-20T10:00:00Z"), categoryId = "food")
        run()
        assertTrue(shown().isEmpty())
    }

    @Test
    fun `no entries at all - no evening reminder`() {
        settings.kinds.value = setOf(NoticeKind.EVENING)
        transactions.items.value = emptyMap()
        run()
        assertTrue(shown().isEmpty())
    }

    @Test
    fun `a limit at eighty percent says what is still available, and wins the day over the evening reminder`() {
        settings.kinds.value = setOf(NoticeKind.LIMIT, NoticeKind.EVENING)
        spend(850)
        run()

        val n = shown().single()
        assertEquals("Продукты: ещё можно 150${MoneyFormat.NBSP}USD", n.extras.getString(NotificationCompat.EXTRA_TITLE))
        assertEquals("Это 15 % лимита на 11 дней.", n.extras.getCharSequence(NotificationCompat.EXTRA_TEXT).toString())
        assertEquals("notice_limit", n.channelId)
        val tap = shadowOf(n.contentIntent).savedIntent
        assertEquals("a tap opens that envelope", "food", tap.getStringExtra(NoticeLinks.EXTRA_CATEGORY))
        assertEquals("2026-11", tap.getStringExtra(NoticeLinks.EXTRA_MONTH))
    }

    @Test
    fun `the same limit is told once a month, the next day the evening reminder takes its turn`() {
        settings.kinds.value = setOf(NoticeKind.LIMIT, NoticeKind.EVENING)
        spend(850)
        run()
        manager.cancelAll()

        now = Instant.parse("2026-11-21T21:00:00Z")
        run()
        assertEquals("Как прошёл день?", shown().single().extras.getString(NotificationCompat.EXTRA_TITLE))
    }

    @Test
    fun `a switched off limit stays silent`() {
        settings.kinds.value = setOf(NoticeKind.EVENING)
        spend(850)
        run()
        assertEquals("Как прошёл день?", shown().single().extras.getString(NotificationCompat.EXTRA_TITLE))
    }

    @Test
    fun `two limits at eighty percent are told one after the other, each once`() {
        settings.kinds.value = setOf(NoticeKind.LIMIT, NoticeKind.EVENING)
        spend(850, cafe = 820)
        run()
        assertTrue(title(shown().single()).orEmpty().startsWith("Продукты"))
        manager.cancelAll()

        now = Instant.parse("2026-11-21T21:00:00Z")
        run()
        assertTrue("the second limit is not lost behind the first", title(shown().single()).orEmpty().startsWith("Кафе"))
        manager.cancelAll()

        now = Instant.parse("2026-11-22T21:00:00Z")
        run()
        assertEquals("both told: the evening reminder takes the day", "Как прошёл день?", title(shown().single()))
    }
}
