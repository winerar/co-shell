package app.coshell.transactions.edit

import app.coshell.core.model.ExchangeRate
import app.coshell.core.model.Money
import app.coshell.core.model.MoneyFormat
import app.coshell.core.model.Rate
import app.coshell.core.model.RateOrigin
import app.coshell.core.model.Transaction
import app.coshell.core.model.TransactionType
import app.coshell.transactions.edit.TransactionEditUiState.KeypadTarget
import kotlin.time.Instant
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** The sheet opened from a receipt QR (#526, boards «QrFilled», «QrForeign», «QrDupB»). */
class TransactionEditScanTest : TransactionEditTestBase() {
    // A real supermarket receipt: 37 914 ARS on 2026-11-19, seller 30-68584975-1.
    private val link = "https://www.afip.gob.ar/fe/qr/?p=eyJ2ZXIiOjEsImN0eiI6MSwidGlwb0RvY1JlYyI6OTksInB0b1Z0YSI6NDQyLCJpbXBvcnRlIjozNzkxNCwidGlwb0NtcCI6NiwiZmVjaGEiOiIyMDI2LTExLTE5IiwiY29kQXV0Ijo4NjQ3MzQ2MjgyODA0OSwiY3VpdCI6MzA2ODU4NDk3NTEsInRpcG9Db2RBdXQiOiJFIiwibnJvQ21wIjoxMTc4MTcsIm5yb0RvY1JlYyI6MCwibW9uZWRhIjoiQVJTIn0="
    private val key = "30685849751:442:6:117817"
    private val receipt = Money(37_914_00, ars)

    @Test
    fun `a peso account gets the amount and the day of the receipt`() = runTest {
        seed()
        val state = viewModel(receiptQr = link).state.value

        assertEquals("card", state.accountId)
        assertEquals(TransactionType.EXPENSE, state.type)
        assertEquals(MoneyFormat.RU.formatForEditing(receipt), state.amount)
        assertFalse(state.receiptEnabled)
        assertEquals(LocalDate(2026, 11, 19), state.date)
        assertEquals(KeypadTarget.AMOUNT, state.keypadTarget)
        assertEquals(ScanUi("30-68584975-1", key), state.scan)
    }

    @Test
    fun `an account in another currency gets the purchase amount instead`() = runTest {
        seed()
        rates.rates.value = listOf(ExchangeRate(LocalDate(2026, 11, 8), usd, ars, Rate.parse("1000"), RateOrigin.DOWNLOADED))
        val vm = viewModel(receiptQr = link)
        vm.onAccountSelect("cash")

        val state = vm.state.value
        assertTrue(state.receiptEnabled)
        assertEquals("ARS", state.receiptCurrency)
        assertEquals(MoneyFormat.RU.formatForEditing(receipt), state.receiptAmount)
        assertEquals(KeypadTarget.RECEIPT, state.keypadTarget)
        // The debited sum follows by the rate: 37 914 ARS at 1 000 = 37,91 USD.
        assertFalse(state.amount.isBlank())

        vm.onAccountSelect("card")
        assertFalse(vm.state.value.receiptEnabled)
        assertEquals(MoneyFormat.RU.formatForEditing(receipt), vm.state.value.amount)
    }

    @Test
    fun `a typed amount stays when another account is picked`() = runTest {
        seed()
        val vm = viewModel(receiptQr = link)
        vm.onAmountChange("100")
        vm.onAccountSelect("cash")
        assertEquals("100", vm.state.value.amount)
        assertFalse(vm.state.value.receiptEnabled)
    }

    @Test
    fun `saving remembers the receipt on a new expense`() = runTest {
        seed()
        val vm = viewModel(receiptQr = link)
        vm.onCategorySelect("cafe")
        vm.save()

        assertTrue(vm.state.value.isDone)
        val saved = transactions.getTransaction("new")!!
        assertEquals(key, saved.receiptKey)
        assertEquals(receipt, saved.amount)
    }

    @Test
    fun `a receipt already recorded opens the window with that operation`() = runTest {
        seed()
        transactions.saveTransaction(
            Transaction("old-1", TransactionType.EXPENSE, "card", receipt, Instant.parse("2026-11-19T17:05:00Z"), categoryId = "cafe", receiptKey = key),
        )
        val duplicate = viewModel(receiptQr = link).state.value.scan!!.duplicate

        assertNotNull(duplicate)
        assertEquals(LocalDate(2026, 11, 19), duplicate!!.date)
        assertEquals("Кафе", duplicate.category)
        assertEquals(MoneyFormat.RU.format(receipt), duplicate.amount)
    }

    @Test
    fun `record anyway closes the window and keeps the form, cancel closes the sheet`() = runTest {
        seed()
        transactions.saveTransaction(Transaction("old-1", TransactionType.EXPENSE, "card", receipt, now, receiptKey = key))

        val anyway = viewModel(receiptQr = link)
        anyway.onScanDuplicateAccept()
        assertNull(anyway.state.value.scan!!.duplicate)
        assertEquals(MoneyFormat.RU.formatForEditing(receipt), anyway.state.value.amount)
        assertFalse(anyway.state.value.isDone)

        val cancel = viewModel(receiptQr = link)
        cancel.onScanDuplicateCancel()
        assertTrue(cancel.state.value.isDone)
    }

    @Test
    fun `a deleted receipt is not a duplicate`() = runTest {
        seed()
        transactions.saveTransaction(Transaction("old-1", TransactionType.EXPENSE, "card", receipt, now, receiptKey = key))
        transactions.deleteTransaction("old-1")
        assertNull(viewModel(receiptQr = link).state.value.scan!!.duplicate)
    }

    @Test
    fun `text without a receipt link leaves an ordinary empty sheet`() = runTest {
        seed()
        val state = viewModel(receiptQr = "https://example.com/").state.value
        assertNull(state.scan)
        assertTrue(state.amount.isBlank())
    }

    @Test
    fun `a shared PDF receipt fills the amount and writes its items into the note`() = runTest {
        seed()
        val text = "Teleshka\tЧЕК\n#4807\nДата\t14.11.2026 09:42\nПельмени Сибирские, 1кг\tx1\t$25.000\nИтого\t$25.000"
        val state = viewModel(receiptQr = text).state.value

        assertEquals(MoneyFormat.RU.formatForEditing(Money(25_000_00, ars)), state.amount)
        assertEquals(LocalDate(2026, 11, 14), state.date)
        assertEquals(ScanUi("Teleshka", "teleshka:4807", document = true), state.scan)
        assertEquals("Teleshka\nПельмени Сибирские, 1кг — ${MoneyFormat.RU.format(Money(25_000_00, ars))}", state.note)
    }

    @Test
    fun `an unread share opens the blank sheet with the window until it is answered`() = runTest {
        seed()
        val vm = viewModel(receiptUnread = true)
        assertTrue(vm.state.value.receiptUnread)
        assertEquals("", vm.state.value.amount)
        vm.onReceiptUnreadDismiss()
        assertFalse(vm.state.value.receiptUnread)
    }


}
