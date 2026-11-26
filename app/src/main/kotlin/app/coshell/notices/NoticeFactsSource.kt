package app.coshell.notices

import app.coshell.backup.auto.AutoBackupSettings
import app.coshell.budgets.BudgetSource
import app.coshell.core.domain.AccountRepository
import app.coshell.core.domain.CategoryRepository
import app.coshell.core.domain.TransactionFilter
import app.coshell.core.domain.TransactionRepository
import app.coshell.core.domain.budgets.budgetPeriodOf
import app.coshell.core.domain.drafts.DraftCheck
import app.coshell.core.domain.drafts.DraftRepository
import app.coshell.core.domain.hints.HintStore
import app.coshell.core.domain.budgets.LimitAlertKind
import app.coshell.core.domain.budgets.LimitAlerts
import app.coshell.core.domain.notices.LimitFact
import app.coshell.core.domain.notices.NoticeFacts
import app.coshell.core.model.MoneyFormat
import javax.inject.Inject
import kotlin.time.Clock
import kotlin.time.Instant
import kotlinx.coroutines.flow.first
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.daysUntil
import kotlinx.datetime.minus
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.todayIn

/** Collects what [app.coshell.core.domain.notices.NoticePlanner] needs from the phone's data (#745): a few small reads, once a day. */
class NoticeFactsSource @Inject constructor(
    private val transactions: TransactionRepository,
    private val accounts: AccountRepository,
    private val categories: CategoryRepository,
    private val drafts: DraftRepository,
    private val budgets: BudgetSource,
    private val hintStore: HintStore,
    private val autoBackup: AutoBackupSettings,
    private val moneyFormat: MoneyFormat,
    private val log: NoticeLog,
    private val clock: Clock,
    private val zone: TimeZone,
) {
    suspend fun collect(): NoticeFacts {
        val today = clock.todayIn(zone)
        val recent = transactions.observeTransactions(TransactionFilter(limit = RECENT)).first()
        val days = recent.map { it.date.toLocalDateTime(zone).date }.filter { it <= today }
        val firstOfMonth = LocalDate(today.year, today.month, 1)
        val lastMonth = transactions.observeTransactions(
            TransactionFilter(from = firstOfMonth.minus(DatePeriod(months = 1)).atStartOfDayIn(zone), to = firstOfMonth.atStartOfDayIn(zone), limit = 1),
        ).first()
        return NoticeFacts(
            today = today,
            hasEntries = recent.isNotEmpty(),
            entryToday = today in days,
            lastEntryDay = days.maxOrNull(),
            firstSeenDay = LocalDate.fromEpochDays(hintStore.firstSeenDay(today.toEpochDays()).toInt()),
            lastBackupDay = autoBackup.config.value.lastBackupAtMs?.let { Instant.fromEpochMilliseconds(it).toLocalDateTime(zone).date },
            limit = limitFact(today),
            lastMonthHadEntries = lastMonth.isNotEmpty(),
            readyDrafts = readyDrafts(),
        )
    }

    /**
     * The limit to tell about (`LimitAlerts`, #186): the nearest to its end among those 80–99 % spent, else the one whose spending
     * runs ahead of the month and would use the limit up early; with what is left and for how long.
     */
    private suspend fun limitFact(today: LocalDate): LimitFact? {
        val snapshot = budgets.observe().first()
        val period = budgetPeriodOf(today, snapshot.startDay)
        val alert = LimitAlerts.pick(snapshot.calculator().month(period.month, today), log.limitTold()) ?: return null
        val name = snapshot.categories.firstOrNull { it.id == alert.progress.budget.categoryId }?.name ?: return null
        return LimitFact(
            key = alert.key,
            name = name,
            left = moneyFormat.copy(trimZeroFraction = true).format(alert.progress.left),
            percentLeft = alert.leftPercent,
            daysLeft = today.daysUntil(period.endExclusive).coerceAtLeast(1),
            pace = alert.kind == LimitAlertKind.PACE,
            categoryId = alert.progress.budget.categoryId,
            month = period.month.toString(),
        )
    }

    private suspend fun readyDrafts(): Int {
        val all = drafts.observeDrafts().first()
        if (all.isEmpty()) return 0
        val accountMap = accounts.observeAccounts(includeArchived = true).first().associateBy { it.id }
        val categoryMap = categories.observeCategories(includeArchived = true).first().associateBy { it.id }
        return all.count { DraftCheck.missing(it, accountMap, categoryMap) == null }
    }

    private companion object {
        /** Enough to know «today» and the last day with an entry; older silence is the same «давно». */
        const val RECENT = 30
    }
}
