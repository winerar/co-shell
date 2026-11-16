package app.coshell.recurring.notify

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import app.coshell.core.domain.recurring.PostPaymentUseCase
import app.coshell.family.sync.SyncScheduler
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus

/**
 * Buttons of «Сегодня платёж: …» that work without opening the app (#405, board `Rec143Notify`):
 * «Провести 650 USD» posts by the template's amount and says «Проведено» with «Отменить»; «Завтра» puts the
 * reminder off for a day ([ReminderLog.snooze]). A payment that cannot post (account archived) keeps its reminder.
 */
@AndroidEntryPoint
class ReminderActionReceiver : BroadcastReceiver() {

    @Inject lateinit var postPayment: PostPaymentUseCase
    @Inject lateinit var notices: PaymentNotices
    @Inject lateinit var log: ReminderLog
    @Inject lateinit var zone: TimeZone

    override fun onReceive(context: Context, intent: Intent) {
        val day = intent.getStringExtra(EXTRA_DAY)?.let { runCatching { LocalDate.parse(it) }.getOrNull() } ?: return
        val ids = intent.getStringArrayExtra(EXTRA_TEMPLATES)?.toList().orEmpty().ifEmpty { return }
        val pending = goAsync()
        scope.launch {
            try {
                when (intent.action) {
                    ACTION_POST -> {
                        val posted = postPayment(ids.single(), day, zone) ?: return@launch
                        notices.cancelReminder()
                        notices.posted(posted)
                        SyncScheduler.syncSoon(context.applicationContext)
                    }
                    ACTION_SNOOZE -> {
                        log.snooze(ids, day.plus(DatePeriod(days = 1)))
                        notices.cancelReminder()
                    }
                }
            } finally {
                pending.finish()
            }
        }
    }

    companion object {
        private const val ACTION_POST = "app.coshell.recurring.POST"
        private const val ACTION_SNOOZE = "app.coshell.recurring.SNOOZE"
        private const val EXTRA_TEMPLATES = "template_ids"
        private const val EXTRA_DAY = "day"
        private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

        fun post(context: Context, templateId: String, day: LocalDate): PendingIntent = intent(context, ACTION_POST, listOf(templateId), day)

        fun snooze(context: Context, templateIds: List<String>, day: LocalDate): PendingIntent = intent(context, ACTION_SNOOZE, templateIds, day)

        private fun intent(context: Context, action: String, ids: List<String>, day: LocalDate): PendingIntent = PendingIntent.getBroadcast(
            context, action.hashCode(),
            Intent(context, ReminderActionReceiver::class.java)
                .setAction(action)
                .putExtra(EXTRA_TEMPLATES, ids.toTypedArray())
                .putExtra(EXTRA_DAY, day.toString()),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
    }
}
