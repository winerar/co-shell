package app.coshell.recurring.notify

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import app.coshell.core.domain.recurring.PostAutoPaymentsUseCase
import app.coshell.core.domain.rates.CurrencyConverter
import app.coshell.core.domain.rates.ExchangeRateRepository
import app.coshell.core.domain.rates.RateBook
import app.coshell.core.domain.recurring.paymentsToRemind
import app.coshell.core.domain.recurring.remindTotal
import app.coshell.core.domain.settings.ReminderSettings
import app.coshell.core.domain.settings.ReportCurrencySettings
import app.coshell.core.model.Money
import app.coshell.core.model.TransactionTemplate
import app.coshell.core.domain.templates.TemplateRepository
import app.coshell.family.sync.SyncScheduler
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlin.time.Clock
import kotlinx.coroutines.flow.first
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

/**
 * Regular payments in the background (#143, [RecurringPaymentsScheduler]): posts due «Проводить само»
 * payments and says «Проведено: …» with «Отменить»; with [KEY_REMIND] (the 9:00 run) also reminds of
 * today's payments that wait for «Провести» — once a day ([ReminderLog]), unless «Напоминания о платежах» is off.
 */
@HiltWorker
class RecurringPaymentsWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val post: PostAutoPaymentsUseCase,
    private val templates: TemplateRepository,
    private val notices: PaymentNotices,
    private val log: ReminderLog,
    private val reminders: ReminderSettings,
    private val reportCurrencies: ReportCurrencySettings,
    private val rates: ExchangeRateRepository,
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
        if (inputData.getBoolean(KEY_REMIND, false) && reminders.enabled.first()) {
            val today = clock.now().toLocalDateTime(zone).date
            if (!log.remindedOn(today)) {
                val due = paymentsToRemind(templates.observeTemplates().first(), today, log.snoozedFor(today))
                notices.remind(due, today, total(due, today))
                log.markReminded(today)
            }
        }
        return Result.success()
    }

    /** «≈ 96 USD» in the first report currency at today's rates; only several payments show it. */
    private suspend fun total(due: List<TransactionTemplate>, today: LocalDate): Money? {
        if (due.size < 2) return null
        val main = reportCurrencies.currencies.first().firstOrNull() ?: ReportCurrencySettings.DEFAULT.first()
        return remindTotal(due, CurrencyConverter(RateBook.of(rates.observeRates().first())), main, today)
    }

    companion object {
        const val KEY_REMIND = "remind"
        private const val TAG = "Recurring"
    }
}
