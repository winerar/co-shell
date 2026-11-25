package app.coshell.budgets.notify

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import app.coshell.budgets.BudgetSource
import app.coshell.core.domain.budgets.LimitAlerts
import app.coshell.core.domain.budgets.budgetPeriodOf
import app.coshell.core.domain.settings.ReminderSettings
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlin.time.Clock
import kotlinx.coroutines.flow.first
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn

/**
 * The daily limit check (#186, scheduled with the payment reminders — `RecurringPaymentsScheduler`, the same hour): unless
 * «Лимиты» is off, at most one notification a day about the limit [LimitAlerts] picks; nothing — nothing is said. No network:
 * the limits are counted from Room.
 */
@HiltWorker
class LimitAlertsWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val budgets: BudgetSource,
    private val settings: ReminderSettings,
    private val log: LimitAlertLog,
    private val notices: LimitNotices,
    private val clock: Clock,
    private val zone: TimeZone,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val today = clock.todayIn(zone)
        if (!settings.limitsEnabled.first() || log.checkedOn(today)) return Result.success()
        val snapshot = budgets.observe().first()
        val month = budgetPeriodOf(today, snapshot.startDay).month
        val alert = LimitAlerts.pick(snapshot.calculator().month(month, today), log.told())
        val name = alert?.let { a -> snapshot.categories.firstOrNull { it.id == a.progress.budget.categoryId }?.name }
        if (alert != null && name != null) notices.show(alert, name)
        log.markChecked(today, alert?.key?.takeIf { name != null }, month.toString())
        return Result.success()
    }
}
