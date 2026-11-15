package app.coshell.recurring.notify

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import app.coshell.MainActivity
import app.coshell.R
import app.coshell.core.domain.AccountRepository
import app.coshell.core.domain.CategoryRepository
import app.coshell.core.domain.recurring.PostedPayment
import app.coshell.core.model.MoneyFormat
import app.coshell.core.model.TransactionTemplate
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

/**
 * The payment notifications (#143, board `Rec143Notify`): «Сегодня платёж: Свет» at 9:00 — several on
 * one day go as one; «Проведено: Netflix · 7 990 ARS» with «Отменить» after a «само» payment. A tap
 * opens the app («Скоро» on «Обзоре» has «Провести»). Silent without the notification permission.
 */
class PaymentNotices @Inject constructor(
    // Already in the app's language, not the phone's (CoShellApp.attachBaseContext).
    @ApplicationContext private val context: Context,
    private val accounts: AccountRepository,
    private val categories: CategoryRepository,
    private val money: MoneyFormat,
) {
    private val manager = NotificationManagerCompat.from(context)

    suspend fun remind(templates: List<TransactionTemplate>) {
        val first = templates.firstOrNull() ?: return
        val (title, text) = if (templates.size == 1) {
            val account = accounts.getAccount(first.accountId)?.name.orEmpty()
            val amount = first.amount
            if (amount != null) {
                context.getString(R.string.recurring_notify_today_amount, first.name, money.format(amount)) to account
            } else {
                context.getString(R.string.recurring_notify_today, first.name) to
                    listOf(account, context.getString(R.string.recurring_notify_amount_later)).filter { it.isNotEmpty() }.joinToString(SEP)
            }
        } else {
            context.resources.getQuantityString(R.plurals.recurring_notify_today_many, templates.size, templates.size) to
                templates.joinToString(", ") { it.name }
        }
        show(REMINDER_ID, builder(title, text).addAction(0, context.getString(R.string.recurring_open), openApp()).build())
    }

    suspend fun posted(payment: PostedPayment) {
        val tx = payment.transaction
        val title = context.getString(R.string.recurring_notify_posted, payment.template.name, money.format(tx.amount))
        val text = listOfNotNull(
            accounts.getAccount(tx.accountId)?.name,
            tx.categoryId?.let { categories.getCategory(it)?.name },
        ).joinToString(SEP)
        val id = postedId(tx.id)
        val notification = builder(title, text)
            .setGroup(GROUP_POSTED)
            .addAction(0, context.getString(R.string.action_undo), UndoPaymentReceiver.intent(context, tx.id, id))
            .addAction(0, context.getString(R.string.recurring_open), openApp())
            .build()
        show(id, notification)
    }

    fun cancel(id: Int) = manager.cancel(id)

    private fun builder(title: String, text: String): NotificationCompat.Builder {
        ensureChannel()
        return NotificationCompat.Builder(context, CHANNEL)
            .setSmallIcon(R.drawable.ic_launcher_monochrome)
            .setContentTitle(title)
            .setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .setContentIntent(openApp())
            .setAutoCancel(true)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
    }

    private fun show(id: Int, notification: Notification) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) return
        manager.notify(id, notification)
    }

    private fun openApp(): PendingIntent = PendingIntent.getActivity(
        context, 0,
        Intent(context, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP),
        PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
    )

    private fun ensureChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val channel = NotificationChannel(CHANNEL, context.getString(R.string.recurring_channel), NotificationManager.IMPORTANCE_DEFAULT)
            .apply { description = context.getString(R.string.recurring_channel_hint) }
        context.getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }

    companion object {
        const val CHANNEL = "recurring_payments"
        private const val GROUP_POSTED = "recurring_posted"
        private const val REMINDER_ID = 143
        private const val SEP = " · "

        /** One notification per posted operation, so «Отменить» knows which one. */
        fun postedId(transactionId: String): Int = transactionId.hashCode()

        fun canNotify(context: Context): Boolean =
            Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
                ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
    }
}
