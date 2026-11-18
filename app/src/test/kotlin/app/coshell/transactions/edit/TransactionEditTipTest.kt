package app.coshell.transactions.edit

import app.coshell.core.domain.quickentry.TipPrefs
import app.coshell.core.designsystem.components.keypad.KeypadKey
import app.coshell.core.model.ExchangeRate
import app.coshell.core.model.Money
import app.coshell.core.model.Rate
import app.coshell.core.model.RateOrigin
import app.coshell.transactions.edit.tip.NEW_TIP_CATEGORY
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** «＋ Чаевые» in the entry sheet (#322, boards `Fb322Tips`, `Fb322First`): two operations, the tip from another account. */
class TransactionEditTipTest : TransactionEditTestBase() {

    private suspend fun readyVm(amount: String = "13 300"): TransactionEditViewModel {
        seed()
        val vm = viewModel()
        vm.onAmountChange(amount)
        vm.onCategorySelect("cafe")
        return vm
    }

    @Test
    fun `the button is for a new expense only`() = runTest {
        seed()
        assertTrue(viewModel().state.value.tipAvailable)
        assertFalse(viewModel().also { it.onRefundToggle(true) }.state.value.tipAvailable)
        assertFalse(viewModel(type = "INCOME").state.value.tipAvailable)
        assertFalse(viewModel(type = "TRANSFER").state.value.tipAvailable)
    }

    @Test
    fun `an invalid purchase shows its errors and does not open the sheet`() = runTest {
        seed()
        val vm = viewModel()
        vm.onTipOpen()
        assertNull(vm.state.value.tip)
        assertEquals(TransactionEditUiState.AmountError.REQUIRED, vm.state.value.amountError)
    }

    @Test
    fun `the first time the category is asked and the account is the purchase's`() = runTest {
        val vm = readyVm()
        vm.onTipOpen()
        val tip = vm.state.value.tip!!
        assertTrue(tip.asking)
        assertNull(tip.categoryId)
        assertEquals("card", tip.accountId)
        assertEquals(10, tip.percent)
        assertFalse(tip.canSave)
        assertTrue(transactions.items.value.isEmpty())
    }

    @Test
    fun `a percent of the receipt is saved as a separate expense next to the purchase`() = runTest {
        val vm = readyVm()
        vm.onTipOpen()
        vm.onTipCategory("cafe", "Чаевые")
        assertTrue(vm.state.value.tip!!.canSave)
        vm.onTipChange { it.copy(percent = 15) }
        vm.onTipSave("Чаевые")

        assertTrue(vm.state.value.isDone)
        val purchase = transactions.getTransaction("new")!!
        assertEquals(Money(13_300_00, ars), purchase.amount)
        val tip = transactions.getTransaction("tip-1")!!
        assertEquals(Money(1_995_00, ars), tip.amount)
        assertEquals("card", tip.accountId)
        assertEquals("cafe", tip.categoryId)
        assertEquals(purchase.date, tip.date)
        assertEquals("Чаевые", tip.note)
        assertNull(tip.originalAmount)
    }

    @Test
    fun `a tip from an account in another currency needs a rate and is converted by it`() = runTest {
        val vm = readyVm()
        vm.onTipOpen()
        vm.onTipCategory("cafe", "Чаевые")
        vm.onTipChange { it.copy(accountId = "cash") }
        assertTrue(vm.state.value.tip!!.noRate)
        assertFalse(vm.state.value.tip!!.canSave)

        rates.rates.value = listOf(ExchangeRate(LocalDate(2026, 11, 8), usd, ars, Rate.parse("1000"), RateOrigin.DOWNLOADED))
        vm.onTipChange { it }
        val tip = vm.state.value.tip!!
        assertFalse(tip.noRate)
        assertNotNull(tip.convertedText)
        vm.onTipSave("Чаевые")

        val saved = transactions.getTransaction("tip-1")!!
        assertEquals("cash", saved.accountId)
        assertEquals(Money(1_33, usd), saved.amount)
        assertEquals(Money(1_330_00, ars), saved.originalAmount)
    }

    @Test
    fun `a typed amount is the tip and Done saves it`() = runTest {
        val vm = readyVm()
        vm.onTipOpen()
        vm.onTipCategory("cafe", "Чаевые")
        vm.onTipChange { it.copy(percent = null) }
        assertFalse(vm.state.value.tip!!.canSave)
        listOf(KeypadKey.Digit('5'), KeypadKey.Digit('0'), KeypadKey.Digit('0')).forEach { vm.onTipKey(it, "Чаевые") }
        assertNull(vm.state.value.tip!!.percent)
        assertTrue(vm.state.value.tip!!.canSave)
        vm.onTipKey(KeypadKey.Done, "Чаевые")

        assertEquals(Money(500_00, ars), transactions.getTransaction("tip-1")!!.amount)
    }

    @Test
    fun `as the purchase puts the tip into the purchase category`() = runTest {
        val vm = readyVm()
        vm.onTipOpen()
        vm.onTipCategory(TipPrefs.SAME_AS_PURCHASE, "Чаевые")
        vm.onTipSave("Чаевые")
        assertEquals("cafe", transactions.getTransaction("tip-1")!!.categoryId)
        assertEquals(TipPrefs.SAME_AS_PURCHASE, tipSettings.prefs.categoryId)
    }

    @Test
    fun `the new category is created once and found by name afterwards`() = runTest {
        val vm = readyVm()
        vm.onTipOpen()
        vm.onTipCategory(NEW_TIP_CATEGORY, "Чаевые")
        vm.onTipSave("Чаевые")
        val created = categories.getCategory("tip-1")!!
        assertEquals("Чаевые", created.name)
        assertEquals("tip-1", tipSettings.prefs.categoryId)
        assertTrue(vm.state.value.categories.any { it.item.id == "tip-1" })

        val again = readyVm()
        again.onTipOpen()
        again.onTipCategory(NEW_TIP_CATEGORY, " чаевые ")
        assertEquals("tip-1", again.state.value.tip!!.categoryId)
    }

    @Test
    fun `the account and the category are remembered for the next time`() = runTest {
        val vm = readyVm()
        vm.onTipOpen()
        vm.onTipCategory("cafe", "Чаевые")
        vm.onTipChange { it.copy(accountId = "cash") }
        rates.rates.value = listOf(ExchangeRate(LocalDate(2026, 11, 8), usd, ars, Rate.parse("1000"), RateOrigin.DOWNLOADED))
        vm.onTipChange { it }
        vm.onTipSave("Чаевые")
        assertEquals(TipPrefs(categoryId = "cafe", accountId = "cash"), tipSettings.prefs)

        val next = readyVm("2 000")
        next.onTipOpen()
        val tip = next.state.value.tip!!
        assertEquals("cash", tip.accountId)
        assertEquals("cafe", tip.categoryId)
        assertFalse(tip.asking)
    }

    @Test
    fun `an account that is gone falls back to the purchase's account`() = runTest {
        tipSettings.prefs = TipPrefs(categoryId = "cafe", accountId = "deleted")
        val vm = readyVm()
        vm.onTipOpen()
        assertEquals("card", vm.state.value.tip!!.accountId)
    }

    @Test
    fun `closing the sheet saves nothing`() = runTest {
        val vm = readyVm()
        vm.onTipOpen()
        vm.onTipClose()
        assertNull(vm.state.value.tip)
        assertTrue(transactions.items.value.isEmpty())
    }

    @Test
    fun `a purchase with a receipt takes the percent of the receipt`() = runTest {
        seed()
        rates.rates.value = listOf(ExchangeRate(LocalDate(2026, 11, 8), usd, ars, Rate.parse("1000"), RateOrigin.DOWNLOADED))
        val vm = viewModel()
        vm.onAccountSelect("cash")
        vm.onReceiptToggle(true)
        vm.onReceiptCurrencyChange("ARS")
        vm.onReceiptAmountChange("13 300")
        vm.onCategorySelect("cafe")
        vm.onTipOpen()
        vm.onTipCategory("cafe", "Чаевые")
        vm.onTipSave("Чаевые")

        // 10 % of 13 300 ARS, paid from the dollar account of the purchase at the day's rate.
        val tip = transactions.getTransaction("tip-1")!!
        assertEquals(Money(1_33, usd), tip.amount)
        assertEquals(Money(1_330_00, ars), tip.originalAmount)
    }
}
