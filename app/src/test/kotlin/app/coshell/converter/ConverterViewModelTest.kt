package app.coshell.converter

import app.coshell.core.designsystem.components.keypad.AmountKeys
import app.coshell.core.designsystem.components.keypad.KeypadKey
import app.coshell.core.domain.settings.ConverterMode
import app.coshell.core.domain.settings.ConverterPick
import app.coshell.core.model.CurrencyCode
import app.coshell.core.model.ExchangeRate
import app.coshell.core.model.MoneyFormat
import app.coshell.core.model.Rate
import app.coshell.core.model.RateOrigin
import app.coshell.testing.FakeConverterSettings
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

class ConverterViewModelTest {
    @get:Rule val main = MainDispatcherRule()

    private val usd = CurrencyCode("USD")
    private val ars = CurrencyCode("ARS")
    private val amd = CurrencyCode("AMD")
    private val nb = MoneyFormat.NARROW_NBSP

    private val repo = FakeExchangeRateRepository().apply {
        used = setOf(usd, ars, amd)
        rates.value = listOf(ExchangeRate(LocalDate(2026, 11, 9), usd, ars, Rate.parse("1460"), RateOrigin.DOWNLOADED))
    }
    private val settings = FakeConverterSettings()
    private val clock = object : Clock {
        override fun now() = Instant.parse("2026-11-10T01:00:00Z")
    }
    private val vm by lazy {
        ConverterViewModel(repo, FakeReportCurrencySettings(), settings, clock, TimeZone.of("America/Argentina/Buenos_Aires"), MoneyFormat.RU)
    }

    private fun type(vararg digits: Char) = digits.forEach { vm.onKey(KeypadKey.Digit(it)) }

    @Test
    fun `opens as a pair of the main currency and the first one with a rate, nothing typed`() {
        val state = vm.state.value
        assertEquals(ConverterMode.PAIR, state.mode)
        assertEquals("USD", state.from)
        assertEquals("ARS", state.to) // AMD has no rate, so it is not the default
        assertTrue(state.isEmpty)
        assertEquals("0", state.fromText)
        assertEquals("0", state.toText)
        assertEquals(listOf("USD", "AMD", "ARS"), state.options)
    }

    @Test
    fun `the first digit is the answer - the other side is converted at once`() {
        type('1', '0', '0')
        val state = vm.state.value
        assertEquals("100", state.fromText)
        assertEquals("146${nb}000", state.toText)
        assertEquals("1\u00A0460", state.rate)
        assertEquals(false, state.isEmpty)
    }

    @Test
    fun `typing on the second side converts back`() {
        vm.onActivate(ConverterSide.TO)
        type('1', '4', '6', '0')
        val state = vm.state.value
        assertEquals(ConverterSide.TO, state.active)
        assertEquals("ARS", state.source)
        assertEquals(AmountKeys.display("1460"), state.toText)
        assertEquals("1", state.fromText)
    }

    @Test
    fun `a pair without a rate shows nothing converted`() {
        vm.onPick(ConverterSide.TO, "AMD")
        type('5')
        assertNull(vm.state.value.toText)
        assertNull(vm.state.value.rate)
        assertEquals(null, vm.state.value.rows.single { it.code == "AMD" }.text)
    }

    @Test
    fun `swap and picks are remembered, the typed number is not`() {
        type('7')
        vm.onSwap()
        assertEquals(ConverterPick(ConverterMode.PAIR, ars, usd), settings.state.value)
        assertEquals("7", vm.state.value.fromText)

        vm.onPick(ConverterSide.TO, "AMD")
        assertEquals(ConverterPick(ConverterMode.PAIR, ars, amd), settings.state.value)
    }

    @Test
    fun `the mode button switches to the list and back, the mode is remembered`() {
        type('1', '0', '0')
        vm.onMode(ConverterMode.ALL)
        var state = vm.state.value
        assertEquals(ConverterMode.ALL, settings.state.value.mode)
        assertEquals(listOf("USD", "AMD", "ARS"), state.rows.map { it.code })
        assertEquals(listOf(true, false, false), state.rows.map { it.isSource })
        assertEquals("146${nb}000", state.rows.single { it.code == "ARS" }.text)

        vm.onSource("ARS")
        state = vm.state.value
        assertEquals("ARS", state.source)
        assertEquals(ars, settings.state.value.from)

        vm.onMode(ConverterMode.PAIR)
        assertEquals(ConverterMode.PAIR, vm.state.value.mode)
    }

    @Test
    fun `opens in the remembered mode and pair`() {
        settings.state.value = ConverterPick(ConverterMode.ALL, ars, usd)
        val state = vm.state.value
        assertEquals(ConverterMode.ALL, state.mode)
        assertEquals("ARS", state.source)
        assertTrue(state.rows.first { it.code == "ARS" }.isSource)
    }

    @Test
    fun `a remembered currency that left the list is replaced`() {
        settings.state.value = ConverterPick(ConverterMode.PAIR, CurrencyCode("EUR"), CurrencyCode("EUR"))
        assertEquals("USD", vm.state.value.from)
        assertEquals("ARS", vm.state.value.to)
    }
}
