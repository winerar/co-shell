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
        assertEquals("1\u00a0060 из 2\u00a0500 МБ · 42 % · осталось 1\u00a0440 МБ".replace('\u00a0', ' '), n.text().replace('\u00a0', ' ').replace('\u202f', ' '))
        assertEquals(42, n.extras.getInt(android.app.Notification.EXTRA_PROGRESS))
        assertEquals(1, NotificationCompat.getActionCount(n))
        assertEquals("Отменить", NotificationCompat.getAction(n, 0)!!.title.toString())
    }

    @Test
    fun `the add-on step shows its megabytes and what is left`() {
        val n = AiDownloadService.notification(context, AiAddonPhase.InstallingAddon(0.4f, 20_000_000))
        assertEquals("8 из 20 МБ · 40 % · осталось 12 МБ", n.text())
        assertEquals(40, n.extras.getInt(android.app.Notification.EXTRA_PROGRESS))
    }

    private fun android.app.Notification.text() = extras.getCharSequence(android.app.Notification.EXTRA_TEXT).toString()

    @Test
    fun `waiting for Android's install dialog is an unmeasured bar`() {
        val n = AiDownloadService.notification(context, AiAddonPhase.InstallingAddon(null))
        assertTrue(n.extras.getBoolean(android.app.Notification.EXTRA_PROGRESS_INDETERMINATE))
        assertEquals("Шаг 1 из 2 · дополнение", n.extras.getCharSequence(android.app.Notification.EXTRA_TEXT).toString())
    }
}
