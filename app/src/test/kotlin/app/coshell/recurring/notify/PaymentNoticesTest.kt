package app.coshell.recurring.notify

import android.app.Application
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.test.core.app.ApplicationProvider
import app.coshell.core.domain.recurring.PostedPayment
import app.coshell.core.model.Account
import app.coshell.core.model.Category
import app.coshell.core.model.CategoryType
import app.coshell.core.model.CurrencyCode
import app.coshell.core.model.Money
import app.coshell.core.model.MoneyFormat
import app.coshell.core.model.Transaction
import app.coshell.core.model.TransactionTemplate
import app.coshell.core.model.TransactionType
import app.coshell.testing.FakeAccountRepository
import app.coshell.testing.FakeCategoryRepository
import kotlin.time.Instant
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

/** Texts and actions of the payment notifications (#143, board `Rec143Notify`). */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "ru", application = Application::class)
class PaymentNoticesTest {
    private val context: Context = ApplicationProvider.getApplicationContext()
    private val ars = CurrencyCode("ARS")
    private val accounts = FakeAccountRepository()
    private val categories = FakeCategoryRepository()
    private val notices = PaymentNotices(context, accounts, categories, MoneyFormat.RU)
    private val manager = context.getSystemService(NotificationManager::class.java)

    private fun template(name: String, amount: Long? = null) =
        TransactionTemplate(name, name, TransactionType.EXPENSE, "galicia", ars, amount?.let { Money(it, ars) })

    @Before
    fun setUp() = runTest {
        shadowOf(context as Application).grantPermissions(android.Manifest.permission.POST_NOTIFICATIONS)
        accounts.saveAccount(Account("galicia", "Galicia", ars))
        categories.saveCategory(Category("subs", "Подписки", CategoryType.EXPENSE))
    }

    private fun shown() = shadowOf(manager).allNotifications

    private fun android.app.Notification.title() = extras.getCharSequence(NotificationCompat.EXTRA_TITLE).toString()
    private fun android.app.Notification.text() = extras.getCharSequence(NotificationCompat.EXTRA_TEXT).toString()

    @Test
    fun `one payment without an amount shows name, account, amount at entry`() = runTest {
        notices.remind(listOf(template("Свет")))
        val n = shown().single()
        assertEquals("Сегодня платёж: Свет", n.title())
        assertEquals("Galicia · сумма при вводе", n.text())
        assertEquals(PaymentNotices.CHANNEL, n.channelId)
    }

    @Test
    fun `one payment with an amount shows it in the title`() = runTest {
        notices.remind(listOf(template("Аренда", 650_000_00)))
        assertEquals("Сегодня платёж: Аренда · 650\u202F000,00\u00A0ARS", shown().single().title())
    }

    @Test
    fun `several payments on one day are one notification`() = runTest {
        notices.remind(listOf(template("Интернет"), template("Expensas"), template("Мобильная связь")))
        val n = shown().single()
        assertEquals("3 платежа сегодня", n.title())
        assertEquals("Интернет, Expensas, Мобильная связь", n.text())
    }

    @Test
    fun `nothing today means no notification`() = runTest {
        notices.remind(emptyList())
        assertTrue(shown().isEmpty())
    }

    @Test
    fun `posted payment shows amount, account and category, with Undo and Open`() = runTest {
        val tx = Transaction(
            "rec-netflix-2026-11-18", TransactionType.EXPENSE, "galicia", Money(7_990_00, ars),
            Instant.parse("2026-11-18T12:00:00Z"), categoryId = "subs",
        )
        notices.posted(PostedPayment(template("Netflix", 7_990_00), tx))
        val n = shown().single()
        assertEquals("Проведено: Netflix · 7 990,00 ARS", n.title())
        assertEquals("Galicia · Подписки", n.text())
        assertEquals(listOf("Отменить", "Открыть"), n.actions.map { it.title.toString() })
    }

    @Test
    fun `no permission means silent`() = runTest {
        shadowOf(context as Application).denyPermissions(android.Manifest.permission.POST_NOTIFICATIONS)
        notices.remind(listOf(template("Свет")))
        assertTrue(shown().isEmpty())
    }
}
