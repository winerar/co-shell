package app.coshell.budgets.notify

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import app.coshell.R
import app.coshell.core.domain.budgets.LimitAlert
import app.coshell.core.domain.budgets.LimitAlertKind
import app.coshell.core.model.MoneyFormat
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

/**
 * The limit notification (#186, board `Not186B`): «Продукты: ещё можно 18 000 ARS» — what is still available first, then a calm
 * word, as the hint `limit_80`; or «Кафе: траты опережают месяц». A tap opens «Бюджеты» on that envelope. Silent without the
 * notification permission.
 */
class LimitNotices @Inject constructor(
    // Already in the app's language, not the phone's (CoShellApp.attachBaseContext).
    @ApplicationContext private val context: Context,
    private val money: MoneyFormat,
) {
    fun show(alert: LimitAlert, categoryName: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) return
        val (title, text) = when (alert.kind) {
            LimitAlertKind.NEAR -> context.getString(R.string.limit_notify_near, categoryName, money.copy(trimZeroFraction = true).format(alert.progress.left)) to
                context.resources.getQuantityString(R.plurals.limit_notify_near_text, alert.daysToGo, alert.leftPercent, alert.daysToGo)
            LimitAlertKind.PACE -> context.getString(R.string.limit_notify_pace, categoryName) to context.getString(R.string.limit_notify_pace_text)
        }
        ensureChannel()
        val link = BudgetLink(alert.progress.budget.categoryId, alert.progress.period.month)
        val notification = NotificationCompat.Builder(context, CHANNEL)
            .setSmallIcon(R.drawable.ic_launcher_monochrome)
            .setContentTitle(title)
            .setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .setContentIntent(BudgetLinks.intent(context, link))
            .setAutoCancel(true)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .build()
        NotificationManagerCompat.from(context).notify(ID, notification)
    }

    private fun ensureChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val channel = NotificationChannel(CHANNEL, context.getString(R.string.limit_channel), NotificationManager.IMPORTANCE_DEFAULT)
            .apply { description = context.getString(R.string.limit_channel_hint) }
        context.getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }

    companion object {
        const val CHANNEL = "budget_limits"

        /** One at a time: a new one replaces yesterday's. */
        const val ID = 186
    }
}
