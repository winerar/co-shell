package app.coshell.recurring.notify

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import app.coshell.core.domain.recurring.PostAutoPaymentsUseCase
import app.coshell.core.domain.recurring.paymentsToRemind
import app.coshell.core.domain.templates.TemplateRepository
import app.coshell.family.sync.SyncScheduler
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlin.time.Clock
import kotlinx.coroutines.flow.first
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

/**
 * Regular payments in the background (#143, [RecurringPaymentsScheduler]): posts due «Проводить само»
 * payments and says «Проведено: …» with «Отменить»; with [KEY_REMIND] (the 9:00 run) also reminds of
 * today's payments that wait for «Провести» — once a day ([ReminderLog]).
 */
@HiltWorker
class RecurringPaymentsWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val post: PostAutoPaymentsUseCase,
    private val templates: TemplateRepository,
    private val notices: PaymentNotices,
    private val log: ReminderLog,
    private val clock: Clock,
    private val zone: TimeZone,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val posted = post(zone)
        if (posted.isNotEmpty()) {
            Log.i(TAG, "Posted ${posted.size} regular payment(s)")
            for (payment in posted) notices.posted(payment)
            SyncScheduler.syncSoon(applicationContext)
        }
        if (inputData.getBoolean(KEY_REMIND, false)) {
            val today = clock.now().toLocalDateTime(zone).date
            if (!log.remindedOn(today)) {
                notices.remind(paymentsToRemind(templates.observeTemplates().first(), today))
                log.markReminded(today)
            }
        }
        return Result.success()
    }

    companion object {
        const val KEY_REMIND = "remind"
        private const val TAG = "Recurring"
    }
}
