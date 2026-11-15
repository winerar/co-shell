package app.coshell.recurring.notify

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import app.coshell.core.domain.TransactionRepository
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * «Отменить» on «Проведено: …» (#143): deletes the operation the payment posted. The payment stays
 * handled — it is not posted again tomorrow; to post it by hand, use the template.
 */
@AndroidEntryPoint
class UndoPaymentReceiver : BroadcastReceiver() {

    @Inject lateinit var transactions: TransactionRepository
    @Inject lateinit var notices: PaymentNotices

    override fun onReceive(context: Context, intent: Intent) {
        val txId = intent.getStringExtra(EXTRA_TX) ?: return
        val notificationId = intent.getIntExtra(EXTRA_NOTIFICATION, 0)
        val pending = goAsync()
        scope.launch {
            try {
                transactions.deleteTransaction(txId)
                notices.cancel(notificationId)
            } finally {
                pending.finish()
            }
        }
    }

    companion object {
        private const val EXTRA_TX = "transaction_id"
        private const val EXTRA_NOTIFICATION = "notification_id"
        private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

        fun intent(context: Context, transactionId: String, notificationId: Int): PendingIntent = PendingIntent.getBroadcast(
            context, notificationId,
            Intent(context, UndoPaymentReceiver::class.java)
                .putExtra(EXTRA_TX, transactionId)
                .putExtra(EXTRA_NOTIFICATION, notificationId),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
    }
}
