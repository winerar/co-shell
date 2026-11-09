package app.coshell.currencies.list

import app.coshell.core.model.CurrencyCode
import app.coshell.testing.FakeExchangeRateRepository
import app.coshell.testing.FakeReportCurrencySettings
import app.coshell.testing.MainDispatcherRule
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class ReportCurrenciesViewModelTest {
    @get:Rule val main = MainDispatcherRule()

    private val usd = CurrencyCode("USD")
    private val ars = CurrencyCode("ARS")
    private val amd = CurrencyCode("AMD")

    private val settings = FakeReportCurrencySettings()
    private val repo = FakeExchangeRateRepository().apply { used = setOf(usd, ars, amd) }
    private val vm by lazy { ReportCurrenciesViewModel(settings, repo) }

    @Test
    fun `add offers used currencies not in the list and appends the chosen one`() {
        vm.openAdd()
        assertEquals(listOf("AMD", "ARS"), vm.state.value.adding!!.suggestions)

        vm.onCodeChange("ars")
        vm.confirmAdd()

        assertNull(vm.state.value.adding)
        assertEquals(listOf(usd, ars), settings.currencies.value)
    }

    @Test
    fun `invalid code keeps the dialog open with an error`() {
        vm.openAdd()
        vm.onCodeChange("$")
        vm.confirmAdd()

        assertTrue(vm.state.value.adding!!.error)
        assertEquals(listOf(usd), settings.currencies.value)
    }

    @Test
    fun `make main and remove edit the list, the last currency stays`() {
        settings.currencies.value = listOf(usd, ars)

        vm.makeMain("ARS")
        assertEquals(listOf(ars, usd), settings.currencies.value)

        vm.remove("USD")
        assertEquals(listOf(ars), settings.currencies.value)
        assertFalse(vm.state.value.canRemove)
        vm.remove("ARS")
        assertEquals(listOf(ars), settings.currencies.value)
    }
}
