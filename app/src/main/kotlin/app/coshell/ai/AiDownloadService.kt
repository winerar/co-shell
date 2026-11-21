package app.coshell.ai

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import app.coshell.R
import dagger.hilt.android.AndroidEntryPoint
import java.text.NumberFormat
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

/**
 * Keeps «ИИ на телефоне» turning on while the app is closed (#587, poll 2026-11-21 «Б»): a foreground service with a notification
 * «Скачиваю ИИ · 42 %» and «Отменить». It only shows [AiAddonController]'s state and stops itself when the work is over.
 */
@AndroidEntryPoint
class AiDownloadService : Service() {
    @Inject lateinit var controller: AiAddonController
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private var watching: Job? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_CANCEL) controller.cancel()
        show(controller.state.value.phase)
        if (watching == null) {
            watching = scope.launch {
                controller.state.collect { state ->
                    if (state.phase is AiAddonPhase.InstallingAddon || state.phase is AiAddonPhase.DownloadingModel) show(state.phase) else stopSelf()
                }
            }
        }
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        scope.cancel()
        super.onDestroy()
    }

    private fun show(phase: AiAddonPhase) {
        val notification = notification(this, phase)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC)
        } else {
            startForeground(ID, notification)
        }
    }

    companion object {
        private const val CHANNEL = "ai_download"
        private const val ID = 1002
        private const val ACTION_CANCEL = "app.coshell.ai.CANCEL"

        /** Called from the user's tap on «Включить» (a foreground start is only allowed while the app is in front). */
        fun start(context: Context) {
            ContextCompat.startForegroundService(context, Intent(context, AiDownloadService::class.java))
        }

        internal fun notification(context: Context, phase: AiAddonPhase): Notification {
            ensureChannel(context)
            val cancel = PendingIntent.getService(
                context, 0, Intent(context, AiDownloadService::class.java).setAction(ACTION_CANCEL),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )
            val (text, percent) = when (phase) {
                is AiAddonPhase.InstallingAddon -> context.getString(R.string.ai_download_step1) to ((phase.progress ?: 0f) * 100).toInt()
                is AiAddonPhase.DownloadingModel -> {
                    val number = NumberFormat.getIntegerInstance()
                    val percent = if (phase.total > 0) (phase.done * 100 / phase.total).toInt() else 0
                    val line = if (phase.total > 0) context.getString(R.string.ai_progress, number.format(phase.done / MB), number.format(phase.total / MB), percent) else context.getString(R.string.ai_step2_title)
                    line to percent
                }
                else -> "" to 0
            }
            val indeterminate = phase is AiAddonPhase.InstallingAddon && phase.progress == null || phase is AiAddonPhase.DownloadingModel && phase.total <= 0
            return NotificationCompat.Builder(context, CHANNEL)
                .setSmallIcon(R.drawable.ic_launcher_monochrome)
                .setContentTitle(context.getString(R.string.ai_download_title))
                .setContentText(text)
                .setProgress(100, percent, indeterminate)
                .setOngoing(true)
                .setOnlyAlertOnce(true)
                .setCategory(NotificationCompat.CATEGORY_PROGRESS)
                .addAction(0, context.getString(R.string.ai_cancel), cancel)
                .build()
        }

        private fun ensureChannel(context: Context) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
            val channel = NotificationChannel(CHANNEL, context.getString(R.string.ai_download_channel), NotificationManager.IMPORTANCE_LOW)
            context.getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }

        private const val MB = 1_000_000L
    }
}
