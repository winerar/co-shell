package app.coshell.budgets.notify

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import app.coshell.MainActivity
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.datetime.YearMonth

/** The envelope of a limit to open from its notification (#186). */
data class BudgetLink(val categoryId: String, val month: YearMonth)

/**
 * Hands a limit from the notification to the navigation, like `PaymentLinks`: `MainActivity` offers its intent,
 * `CoShellNavHost` opens «Бюджеты» on that envelope and says [consumed].
 */
@Singleton
class BudgetLinks @Inject constructor() {
    private val pending = MutableStateFlow<BudgetLink?>(null)
    val link: StateFlow<BudgetLink?> = pending.asStateFlow()

    /** `true` — the intent carried a limit. */
    fun offer(intent: Intent?): Boolean {
        val link = parse(intent) ?: return false
        pending.value = link
        // Opened once: a recreated activity sees the same intent again.
        intent?.removeExtra(EXTRA_CATEGORY)
        return true
    }

    fun consumed() {
        pending.value = null
    }

    companion object {
        private const val EXTRA_CATEGORY = "budget_category_id"
        private const val EXTRA_MONTH = "budget_month"

        fun parse(intent: Intent?): BudgetLink? {
            val category = intent?.getStringExtra(EXTRA_CATEGORY) ?: return null
            val month = intent.getStringExtra(EXTRA_MONTH)?.let { runCatching { YearMonth.parse(it) }.getOrNull() } ?: return null
            return BudgetLink(category, month)
        }

        fun intent(context: Context, link: BudgetLink): PendingIntent = PendingIntent.getActivity(
            context, link.hashCode(),
            Intent(context, MainActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                .putExtra(EXTRA_CATEGORY, link.categoryId)
                .putExtra(EXTRA_MONTH, link.month.toString()),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
    }
}
