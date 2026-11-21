package app.coshell.ai

import android.app.Application
import androidx.core.app.NotificationCompat
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/** The notification of the AI download (#587, poll 2026-11-21 «Б»): the words, the percent, «Отменить». */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "ru", application = Application::class)
class AiDownloadNotificationTest {
    private val context: Application get() = ApplicationProvider.getApplicationContext()

    @Test
    fun `the model step shows megabytes and percent with a cancel button`() {
        val n = AiDownloadService.notification(context, AiAddonPhase.DownloadingModel(1_060_000_000, 2_500_000_000))
        assertEquals("Скачиваю ИИ", n.extras.getString(android.app.Notification.EXTRA_TITLE))
        assertTrue(n.extras.getCharSequence(android.app.Notification.EXTRA_TEXT).toString().contains("42"))
        assertEquals(42, n.extras.getInt(android.app.Notification.EXTRA_PROGRESS))
        assertEquals(1, NotificationCompat.getActionCount(n))
        assertEquals("Отменить", NotificationCompat.getAction(n, 0)!!.title.toString())
    }

    @Test
    fun `waiting for Android's install dialog is an unmeasured bar`() {
        val n = AiDownloadService.notification(context, AiAddonPhase.InstallingAddon(null))
        assertTrue(n.extras.getBoolean(android.app.Notification.EXTRA_PROGRESS_INDETERMINATE))
        assertEquals("Шаг 1 из 2 · дополнение", n.extras.getCharSequence(android.app.Notification.EXTRA_TEXT).toString())
    }
}
