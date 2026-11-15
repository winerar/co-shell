package app.coshell.recurring.notify

import android.app.Application
import android.content.Context
import android.content.Intent
import androidx.test.core.app.ApplicationProvider
import kotlinx.datetime.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

/** «Изменить сумму» opens the sheet on the payment, «Завтра» asks again the next day (#405). */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], application = Application::class)
class ReminderActionsTest {
    private val context: Context = ApplicationProvider.getApplicationContext()
    private val day = LocalDate(2026, 11, 15)

    @Test
    fun `the notification intent carries the payment to the sheet once`() {
        val pending = PaymentLinks.intent(context, PaymentLink("light", day))
        val intent = shadowOf(pending).savedIntent
        val links = PaymentLinks()

        assertTrue(links.offer(intent))
        assertEquals(PaymentLink("light", day), links.link.value)
        // A recreated activity sees the same intent: the sheet does not open twice.
        assertFalse(links.offer(intent))
        links.consumed()
        assertNull(links.link.value)
    }

    @Test
    fun `an ordinary launch carries no payment`() {
        val links = PaymentLinks()
        assertFalse(links.offer(Intent(Intent.ACTION_MAIN)))
        assertFalse(links.offer(null))
        assertNull(links.link.value)
    }

    @Test
    fun `tomorrow is remembered for that day only`() {
        val log = ReminderLog(context)
        log.snooze(listOf("light", "gas"), day)

        assertEquals(setOf("light", "gas"), log.snoozedFor(day))
        assertEquals(emptySet<String>(), log.snoozedFor(LocalDate(2026, 11, 16)))
    }
}
