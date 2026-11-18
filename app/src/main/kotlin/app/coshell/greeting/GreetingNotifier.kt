package app.coshell.greeting

import android.Manifest
import android.annotation.SuppressLint
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
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/** Sends the system notification of the 1.0 greeting. */
interface GreetingNotices {
    /** Returns `true` when the notification was shown (no permission → `false`, the pill still greets). */
    fun show(): Boolean
}

/** A tap on the notification opens the app on «Обзор» (the pill waits there). */
@Singleton
class GreetingNotifier @Inject constructor(@ApplicationContext private val context: Context) : GreetingNotices {

    @SuppressLint("MissingPermission") // checked by canNotify()
    override fun show(): Boolean {
        if (!canNotify()) return false
        ensureChannel()
        val open = PendingIntent.getActivity(
            context, 0, Intent(context, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
        val notification = NotificationCompat.Builder(context, CHANNEL)
            .setSmallIcon(R.drawable.ic_launcher_monochrome)
            .setContentTitle(context.getString(R.string.greeting_notification_title))
            .setContentText(context.getString(R.string.greeting_notification_text))
            .setStyle(NotificationCompat.BigTextStyle().bigText(context.getString(R.string.greeting_notification_text)))
            .setContentIntent(open)
            .setAutoCancel(true)
            .setCategory(NotificationCompat.CATEGORY_SOCIAL)
            .build()
        NotificationManagerCompat.from(context).notify(ID, notification)
        return true
    }

    private fun canNotify(): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED

    private fun ensureChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val channel = NotificationChannel(CHANNEL, context.getString(R.string.greeting_channel), NotificationManager.IMPORTANCE_DEFAULT)
        context.getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }

    private companion object {
        const val CHANNEL = "greeting"
        const val ID = 1001
    }
}
