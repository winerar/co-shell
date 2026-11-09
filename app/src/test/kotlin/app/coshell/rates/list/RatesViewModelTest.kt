package app.coshell.rates.list

import app.coshell.core.model.CurrencyCode
import app.coshell.core.model.ExchangeRate
import app.coshell.core.model.Rate
import app.coshell.core.model.RateOrigin
import app.coshell.rates.RefreshStatus
import app.coshell.testing.FakeExchangeRateRepository
import app.coshell.testing.FakeReportCurrencySettings
import app.coshell.testing.MainDispatcherRule
import kotlin.time.Clock
import kotlin.time.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class RatesViewModelTest {
    @get:Rule val main = MainDispatcherRule()

    private val usd = CurrencyCode("USD")
    private val ars = CurrencyCode("ARS")
    private val amd = CurrencyCode("AMD")
    private val nov5 = LocalDate(2026, 11, 5)
    private val nov9 = LocalDate(2026, 11, 9)

    private val repo = FakeExchangeRateRepository().apply {
        used = setOf(usd, ars, amd)
        rates.value = listOf(ExchangeRate(nov9, usd, ars, Rate.parse("1460"), RateOrigin.DOWNLOADED))
    }
    private val refresher = FakeRatesRefresher()
    private val settings = FakeReportCurrencySettings()

    // 2026-11-10 01:00 UTC is still 9 November in Buenos Aires.
    private val clock = object : Clock {
        override fun now() = Instant.parse("2026-11-10T01:00:00Z")
    }
    // Lazy: created inside the test, after MainDispatcherRule has set the main dispatcher.
    private val vm by lazy { RatesViewModel(repo, refresher, settings, clock, TimeZone.of("America/Argentina/Buenos_Aires")) }

    private fun manual(date: LocalDate, rate: String) = ExchangeRate(date, usd, ars, Rate.parse(rate), RateOrigin.MANUAL)

    @Test
    fun `current rates list used currencies against USD`() {
        val current = vm.state.value.current
        assertEquals(listOf("AMD", "ARS"), current.map { it.currency })
        assertNull(current[0].rateText)
        assertEquals("1\u00A0460", current[1].rateText)
        assertEquals(nov9, current[1].date)
    }

    @Test
    fun `current rates follow the main currency`() {
        settings.currencies.value = listOf(ars, usd)

        val state = vm.state.value
        assertEquals("ARS", state.base)
        assertEquals(listOf("AMD", "USD"), state.current.map { it.currency })
        assertEquals("0,0006849", state.current[1].rateText) // 1 ARS = 1/1460 USD
        vm.addManual()
        assertEquals("ARS", vm.state.value.editor!!.from)
    }

    @Test
    fun `a tiny rate like USD to BTC is shown instead of crashing`() {
        val btc = CurrencyCode("BTC")
        repo.used = setOf(usd, btc)
        repo.rates.value = listOf(ExchangeRate(nov9, usd, btc, Rate.parse("0.0000098766"), RateOrigin.DOWNLOADED))

        assertEquals("0,000009877", vm.state.value.current.single().rateText)
    }

    @Test
    fun `adding a manual rate from a current row saves it for today`() {
        vm.setManual(vm.state.value.current[1])
        assertEquals("1\u00A0460", vm.state.value.editor!!.rate)
        assertEquals(nov9, vm.state.value.editor!!.date)

        vm.onRateChange("1 500")
        vm.saveManual()

        assertNull(vm.state.value.editor)
        assertEquals(listOf(manual(nov9, "1500")), vm.state.value.manual.map { it.rate })
        val ars = vm.state.value.current[1]
        assertEquals("1\u00A0500", ars.rateText)
        assertTrue(ars.isManual)
    }

    @Test
    fun `invalid form stays open with errors`() {
        vm.addManual()
        vm.onToChange("usd")
        vm.saveManual()

        val editor = vm.state.value.editor!!
        assertEquals(ManualRateForm.CurrencyError.SAME, editor.toError)
        assertTrue(editor.rateError)
    }

    @Test
    fun `moving an edited rate to another day removes the old one`() {
        repo.rates.value += manual(nov5, "1400")
        vm.editManual(vm.state.value.manual.single())
        assertEquals("1400", vm.state.value.editor!!.rate)

        vm.onDateSelect(nov9)
        vm.saveManual()

        assertEquals(listOf(manual(nov9, "1400")), vm.state.value.manual.map { it.rate })
    }

    @Test
    fun `delete asks first`() {
        repo.rates.value += manual(nov5, "1400")
        vm.askDelete(vm.state.value.manual.single())
        assertEquals(1, vm.state.value.manual.size)

        vm.confirmDelete()
        assertTrue(vm.state.value.manual.isEmpty())
        assertNull(vm.state.value.pendingDelete)
    }

    @Test
    fun `refresh result is shown only after pressing the button here`() {
        refresher.status.value = RefreshStatus.Failed
        assertEquals(RefreshStatus.Idle, vm.state.value.refresh)

        vm.refreshNow()
        assertEquals(1, refresher.runs)
        assertEquals(RefreshStatus.Running, vm.state.value.refresh)
        refresher.status.value = RefreshStatus.Updated
        assertEquals(RefreshStatus.Updated, vm.state.value.refresh)
    }
}
