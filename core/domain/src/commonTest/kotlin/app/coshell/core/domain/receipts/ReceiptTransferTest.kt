package app.coshell.core.domain.receipts

import app.coshell.core.model.Account
import app.coshell.core.model.CurrencyCode
import app.coshell.core.model.Money
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlinx.datetime.LocalDate

/** Sample: `docs/samples/receipts/ameriabank-own-transfer.txt` (text layer of the PDF, numbers replaced). */
class ReceiptTransferTest {
    private val amd = CurrencyCode("AMD")
    private val own =
        "+374 10 56 11 11\ninfo@ameriabank.am\nПеревод между своими счетами*\nДата создания\t2026-11-23 13:26:55\n" +
        "Счёт списания\t4000********0001\nСчёт зачисления\t4000********0002\nСумма списания\t3,000.00 AMD\n" +
        "Сумма зачисления\t3,000.00 AMD\nДетали\tTransfer of own funds\nСтатус\tПодтверждено\nДата подтверждения\t2026-11-23 13:27:06"

    private fun account(id: String, digits: String?, archived: Boolean = false) =
        Account(id = id, name = id, currency = amd, cardDigits = digits, archived = archived)

    @Test
    fun `own transfer reads both accounts, the sums and the day`() {
        val r = AmeriabankTransferFormat.read(own)!!
        assertEquals(Money(300_000, amd), r.total)
        assertEquals(LocalDate(2026, 11, 23), r.date)
        assertEquals("0001", r.debitDigits)
        assertEquals(ReceiptTransfer("0002", Money(300_000, amd)), r.transfer)
        assertEquals("ameriabank-own:20261123132655:0001:0002:300000", r.key)
    }

    @Test
    fun `own transfer is found by the shared reader and is not read as a card payment`() {
        assertEquals(AmeriabankTransferFormat.id, ReceiptFormats.read(own)!!.format)
        assertNull(AmeriabankFormat.read(own))
    }

    @Test
    fun `own transfer with another credited sum keeps both`() {
        val r = AmeriabankTransferFormat.read(own.replace("Сумма зачисления\t3,000.00 AMD", "Сумма зачисления\t7.50 USD"))!!
        assertEquals(Money(750, CurrencyCode("USD")), r.transfer!!.toTotal)
    }

    @Test
    fun `own transfer without an account, a sum or the day is no receipt`() {
        assertNull(AmeriabankTransferFormat.read(own.replace("Счёт зачисления\t4000********0002\n", "")))
        assertNull(AmeriabankTransferFormat.read(own.replace("Сумма списания\t3,000.00 AMD\n", "")))
        assertNull(AmeriabankTransferFormat.read(own.replace("Дата создания\t2026-11-23 13:26:55\n", "")))
        assertNull(AmeriabankTransferFormat.read(own.replace("ameriabank", "other").replace("Америабанк", "")))
        assertNull(AmeriabankTransferFormat.read(own.replace("между своими счетами", "карточный")))
    }

    @Test
    fun `card payments carry the debit digits of the account`() {
        val ameria = "info@ameriabank.am\nКарточный перевод\nДата утверждения\t26 Октябрь 2026\nПолучатель\tVISA\nСчёт списания\t1570000000000001\n" +
            "Сумма\t2 733.85 AMD\nДетали\tՔ: Spotify P4856C4BE6 STOCKHOLM 75"
        assertEquals("0001", AmeriabankFormat.read(ameria)!!.debitDigits)
        val tbank = "14.11.2026 20:04:28\nИтого\t12 337 ₽\nСтатус\tУспешно\nСумма\t12 337 ₽\nПолучатель\tМария К.\n" +
            "Идентификатор операции\tB63181704291760D0G101600118\nСчет списания\t408170000000****4321\nКвитанция № 1\nfb@tbank.ru"
        assertEquals("4321", TbankFormat.read(tbank)!!.debitDigits)
    }

    @Test
    fun `an account is found by its digits only when it is the single live one`() {
        val a = account("a", "0001")
        val b = account("b", "0002")
        assertEquals(a, ReceiptAccounts.find(listOf(a, b), "0001"))
        assertNull(ReceiptAccounts.find(listOf(a, b), "9999"))
        assertNull(ReceiptAccounts.find(listOf(a, b), null))
        assertNull(ReceiptAccounts.find(listOf(a, account("a2", "0001")), "0001"))
        assertEquals(b, ReceiptAccounts.find(listOf(account("old", "0002", archived = true), b), "0002"))
    }

    @Test
    fun `digits are the last four numbers of what the bank prints`() {
        assertEquals("3900", Account.digitsOf("4083********3900"))
        assertEquals("0001", Account.digitsOf("1570 0000 0000 0001"))
        assertEquals("3900", Account.digitsOf("3900"))
        assertNull(Account.digitsOf("390"))
        assertNull(Account.digitsOf("****"))
    }
}
