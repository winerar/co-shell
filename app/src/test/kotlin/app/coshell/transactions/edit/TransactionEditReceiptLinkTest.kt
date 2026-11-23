package app.coshell.transactions.edit

import app.coshell.core.model.Account
import app.coshell.core.model.CurrencyCode
import app.coshell.core.model.Money
import app.coshell.core.model.MoneyFormat
import app.coshell.core.model.TransactionType
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** A bank receipt finds its account by the card digits (#577) and a receipt of a transfer between own accounts opens as «Перевод» (#646). */
class TransactionEditReceiptLinkTest : TransactionEditTestBase() {
    private val amd = CurrencyCode("AMD")

    private val own =
        "info@ameriabank.am\nПеревод между своими счетами*\nДата создания\t2026-11-23 13:26:55\n" +
        "Счёт списания\t4000********0001\nСчёт зачисления\t4000********0002\nСумма списания\t3,000.00 AMD\n" +
        "Сумма зачисления\t3,000.00 AMD\nДетали\tTransfer of own funds\nСтатус\tПодтверждено"

    private val payment =
        "info@ameriabank.am\nКарточный перевод\nДата утверждения\t26 Октябрь 2026\nПолучатель\tVISA INTERNATIONAL\n" +
        "Счёт списания\t4000000000000001\nСумма\t2 733.85 AMD\nДетали\tՔ: Spotify P4856C4BE6 STOCKHOLM 75"

    private suspend fun seedAmd(first: String? = "0001", second: String? = "0002") {
        seed()
        accounts.saveAccount(Account("amd1", "Ameria · карта", amd, sortOrder = 2, cardDigits = first))
        accounts.saveAccount(Account("amd2", "Ameria · накопит.", amd, sortOrder = 3, cardDigits = second))
    }

    @Test
    fun `a transfer receipt opens as a transfer between the accounts with those digits`() = runTest {
        seedAmd()
        val state = viewModel(receiptQr = own).state.value

        assertEquals(TransactionType.TRANSFER, state.type)
        assertEquals("amd1", state.accountId)
        assertEquals("amd2", state.toAccountId)
        assertEquals(MoneyFormat.RU.formatForEditing(Money(300_000, amd)), state.amount)
        assertEquals("", state.toAmount)
        assertEquals(LocalDate(2026, 11, 23), state.date)
        assertEquals("ameriabank-own:20261123132655:0001:0002:300000", state.scan?.key)
        assertNull(state.receiptAccountAsk)
    }

    @Test
    fun `a transfer between known accounts is saved with the receipt key`() = runTest {
        seedAmd()
        val vm = viewModel(receiptQr = own)
        vm.save()

        assertTrue(vm.state.value.isDone)
        val saved = transactions.getTransaction("new")!!
        assertEquals(TransactionType.TRANSFER, saved.type)
        assertEquals("amd2", saved.toAccountId)
        assertEquals("ameriabank-own:20261123132655:0001:0002:300000", saved.receiptKey)
    }

    @Test
    fun `unknown digits ask which account, remember the answer and then ask for the other side`() = runTest {
        seedAmd(first = null, second = null)
        val vm = viewModel(receiptQr = own)

        assertEquals(TransactionType.TRANSFER, vm.state.value.type)
        assertEquals(ReceiptAccountAsk("0001", target = false), vm.state.value.receiptAccountAsk)

        vm.onReceiptAccountPick("amd1")
        assertEquals("0001", accounts.getAccount("amd1")!!.cardDigits)
        assertEquals("amd1", vm.state.value.accountId)
        assertEquals(ReceiptAccountAsk("0002", target = true), vm.state.value.receiptAccountAsk)

        vm.onReceiptAccountPick("amd2")
        assertEquals("0002", accounts.getAccount("amd2")!!.cardDigits)
        assertEquals("amd2", vm.state.value.toAccountId)
        assertNull(vm.state.value.receiptAccountAsk)
    }

    @Test
    fun `closing the question keeps the transfer and remembers nothing`() = runTest {
        seedAmd(first = null, second = null)
        val vm = viewModel(receiptQr = own)
        vm.onReceiptAccountSkip()

        assertNull(vm.state.value.receiptAccountAsk)
        assertEquals(TransactionType.TRANSFER, vm.state.value.type)
        assertNull(accounts.getAccount("amd1")!!.cardDigits)
    }

    @Test
    fun `a card payment chooses the account with the debit digits`() = runTest {
        seedAmd()
        val state = viewModel(receiptQr = payment).state.value

        assertEquals(TransactionType.EXPENSE, state.type)
        assertEquals("amd1", state.accountId)
        assertNull(state.receiptAccountAsk)
    }

    @Test
    fun `a card payment with unknown digits asks and the answer chooses the account`() = runTest {
        seedAmd(first = null, second = null)
        val vm = viewModel(receiptQr = payment)
        assertEquals(ReceiptAccountAsk("0001"), vm.state.value.receiptAccountAsk)

        vm.onReceiptAccountPick("amd2")
        assertEquals("0001", accounts.getAccount("amd2")!!.cardDigits)
        assertEquals("amd2", vm.state.value.accountId)
        assertNull(vm.state.value.receiptAccountAsk)
    }

    @Test
    fun `an archived account or two accounts with the same digits do not decide`() = runTest {
        seedAmd(first = "0001", second = "0001")
        assertEquals(ReceiptAccountAsk("0001"), viewModel(receiptQr = payment).state.value.receiptAccountAsk)
    }
}
