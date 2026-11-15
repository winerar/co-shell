package app.coshell.recurring.notify

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import app.coshell.MainActivity
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.datetime.LocalDate

/** A payment to open in the «Провести» sheet: «Изменить сумму» or «Провести» without an amount on the reminder (#405). */
data class PaymentLink(val templateId: String, val date: LocalDate)

/**
 * Hands a payment from the notification to the navigation: `MainActivity` offers its intent, `CoShellNavHost` opens
 * the sheet and says [consumed]. Not a deep link: the app has no public URLs, and the sheet needs the app's state.
 */
@Singleton
class PaymentLinks @Inject constructor() {
    private val pending = MutableStateFlow<PaymentLink?>(null)
    val link: StateFlow<PaymentLink?> = pending.asStateFlow()

    /** `true` — the intent carried a payment. */
    fun offer(intent: Intent?): Boolean {
        val link = parse(intent) ?: return false
        pending.value = link
        // Opened once: a recreated activity sees the same intent again.
        intent?.removeExtra(EXTRA_TEMPLATE)
        return true
    }

    fun consumed() {
        pending.value = null
    }

    companion object {
        private const val EXTRA_TEMPLATE = "payment_template_id"
        private const val EXTRA_DATE = "payment_date"

        fun parse(intent: Intent?): PaymentLink? {
            val templateId = intent?.getStringExtra(EXTRA_TEMPLATE) ?: return null
            val date = intent.getStringExtra(EXTRA_DATE)?.let { runCatching { LocalDate.parse(it) }.getOrNull() } ?: return null
            return PaymentLink(templateId, date)
        }

        fun intent(context: Context, link: PaymentLink): PendingIntent = PendingIntent.getActivity(
            context, link.hashCode(),
            Intent(context, MainActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                .putExtra(EXTRA_TEMPLATE, link.templateId)
                .putExtra(EXTRA_DATE, link.date.toString()),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
    }
}
