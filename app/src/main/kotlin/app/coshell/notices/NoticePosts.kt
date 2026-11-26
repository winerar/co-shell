package app.coshell.notices

import android.Manifest
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
import app.coshell.core.domain.notices.NoticeKind
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/** What one notification says and where it leads (#745). Texts come from [NoticeTexts]; the action is the tap on the body too. */
data class NoticeContent(val title: String, val text: String, val target: NoticeTarget, val actionLabel: String)

/**
 * Posts the optional notifications (#745): a channel per kind so the person can silence one kind in the phone's settings,
 * one notification per kind, a tap (or the single action) opens the screen through [NoticeLinks].
 */
@Singleton
class NoticePosts @Inject constructor(@ApplicationContext private val context: Context) {
    private val manager = NotificationManagerCompat.from(context)

    /** `false` — the system does not let us notify (Android 13+ permission), nothing was posted. */
    fun show(kind: NoticeKind, content: NoticeContent): Boolean {
        ensureChannel(kind)
        val open = open(kind, content.target)
        val notification = NotificationCompat.Builder(context, channelId(kind))
            .setSmallIcon(R.drawable.ic_launcher_monochrome)
            .setContentTitle(content.title)
            .setContentText(content.text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(content.text))
            .setContentIntent(open)
            .addAction(0, content.actionLabel, open)
            .setAutoCancel(true)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .build()
        // lint does not see a check made in another function: the permission is read right before `notify`.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) return false
        manager.notify(notificationId(kind), notification)
        return true
    }

    private fun open(kind: NoticeKind, target: NoticeTarget): PendingIntent = PendingIntent.getActivity(
        context, notificationId(kind),
        Intent(context, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP).putExtra(NoticeLinks.EXTRA, target.name),
        PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
    )

    private fun ensureChannel(kind: NoticeKind) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val name = context.getString(channelName(kind))
        val channel = NotificationChannel(channelId(kind), name, NotificationManager.IMPORTANCE_DEFAULT)
        context.getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }

    private companion object {
        const val BASE_ID = 7450

        fun channelId(kind: NoticeKind) = "notice_${kind.key}"
        fun notificationId(kind: NoticeKind) = BASE_ID + kind.ordinal

        fun channelName(kind: NoticeKind): Int = when (kind) {
            NoticeKind.EVENING -> R.string.notice_channel_evening
            NoticeKind.LIMIT -> R.string.notice_channel_limit
            NoticeKind.RECAP -> R.string.notice_channel_recap
            NoticeKind.BACKUP -> R.string.notice_channel_backup
            NoticeKind.DRAFTS -> R.string.notice_channel_drafts
            NoticeKind.COMEBACK -> R.string.notice_channel_comeback
        }
    }
}
