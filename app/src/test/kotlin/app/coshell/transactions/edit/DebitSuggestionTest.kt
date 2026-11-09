package app.coshell.transactions.edit

import androidx.lifecycle.SavedStateHandle
import app.coshell.core.domain.SaveTransactionUseCase
import app.coshell.core.model.Account
import app.coshell.core.model.CurrencyCode
import app.coshell.core.model.ExchangeRate
import app.coshell.core.model.IdGenerator
import app.coshell.core.model.MoneyFormat
import app.coshell.core.model.Rate
import app.coshell.core.model.RateOrigin
import app.coshell.navigation.TransactionEditDestination
import app.coshell.transactions.DeleteTransactionWithUndo
import app.coshell.undo.UndoCenter
import app.coshell.testing.FakeAccountRepository
import app.coshell.testing.FakeCategoryRepository
import app.coshell.testing.FakeExchangeRateRepository
import app.coshell.testing.FakeTransactionRepository
import app.coshell.testing.MainDispatcherRule
import kotlin.time.Clock
import kotlin.time.Instant
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/** "Receipt in another currency": the debited amount is suggested from the receipt by the known rate. */
class DebitSuggestionTest {
    @get:Rule val main = MainDispatcherRule()

    private val zone = TimeZone.of("America/Argentina/Buenos_Aires")
    private val usd = CurrencyCode("USD")
    private val ars = CurrencyCode("ARS")
    private val usdt = CurrencyCode("USDT")
    private val nov7 = LocalDate(2026, 11, 7)

    private val accounts = FakeAccountRepository()
    private val categories = FakeCategoryRepository()
    private val transactions = FakeTransactionRepository()
    private val rates = FakeExchangeRateRepository()

    private suspend fun viewModel(): TransactionEditViewModel {
        accounts.saveAccount(Account("card", "Bybit", usdt, sortOrder = 0))
        accounts.saveAccount(Account("cash", "Песо", ars, sortOrder = 1))
        rates.rates.value = listOf(
            ExchangeRate(nov7, usd, ars, Rate.parse("1500"), RateOrigin.DOWNLOADED),
            ExchangeRate(nov7, usd, usdt, Rate.parse("1"), RateOrigin.DOWNLOADED),
        )
        return TransactionEditViewModel(
            savedStateHandle = SavedStateHandle(mapOf(TransactionEditDestination.ARG_TYPE to "EXPENSE")),
            accounts = accounts,
            categories = categories,
            transactions = transactions,
            saveTransaction = SaveTransactionUseCase(accounts, categories, transactions),
            ids = IdGenerator { "new" },
            moneyFormat = MoneyFormat.RU,
            clock = object : Clock {
                override fun now(): Instant = LocalDateTime(2026, 11, 8, 14, 30).toInstant(zone)
            },
            timeZone = zone,
            rates = rates,
            deleteWithUndo = DeleteTransactionWithUndo(transactions, UndoCenter()),
        ).also { it.onReceiptToggle(true) } // account "card" (USDT), receipt in ARS
    }

    @Test
    fun `debited amount follows the receipt by the cross rate`() = runTest {
        val vm = viewModel()
        vm.onReceiptAmountChange("15 000")
        val state = vm.state.value
        assertEquals("10", state.amount)
        assertTrue(state.amountSuggested)
        assertEquals(nov7, state.suggestedRateDate)

        vm.onReceiptAmountChange("30 000")
        assertEquals("20", vm.state.value.amount)
        vm.onReceiptAmountChange("")
        assertEquals("", vm.state.value.amount)
    }

    @Test
    fun `typed debited amount is never overwritten`() = runTest {
        val vm = viewModel()
        vm.onReceiptAmountChange("15 000")
        vm.onAmountChange("10,05")
        assertFalse(vm.state.value.amountSuggested)
        vm.onReceiptAmountChange("16 000")
        assertEquals("10,05", vm.state.value.amount)
    }

    @Test
    fun `turning the receipt off removes the suggestion`() = runTest {
        val vm = viewModel()
        vm.onReceiptAmountChange("15 000")
        vm.onReceiptToggle(false)
        assertEquals("", vm.state.value.amount)
        assertFalse(vm.state.value.amountSuggested)
    }

    @Test
    fun `no rate known - nothing is suggested`() = runTest {
        val vm = viewModel()
        rates.rates.value = emptyList()
        vm.onReceiptAmountChange("15 000")
        assertEquals("", vm.state.value.amount)
    }
}
