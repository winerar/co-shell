package app.coshell.core.domain.importer.wallet

import app.coshell.core.model.CurrencyCode
import app.coshell.core.model.Money
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class WalletDecimalTest {

    private val usd = CurrencyCode("USD")
    private val jpy = CurrencyCode("JPY")
    private val kwd = CurrencyCode("KWD")

    @Test
    fun parsesWalletNumbers() {
        assertEquals(WalletDecimal(-1300, 2), WalletDecimal.parse("-13.00"))
        assertEquals(WalletDecimal(1500, 0), WalletDecimal.parse("1500"))
        assertEquals(WalletDecimal(5, 1), WalletDecimal.parse(" 0.5 "))
    }

    @Test
    fun rejectsAnythingButPlainDotDecimals() {
        listOf("", "-", "1,5", "1 000.00", "1.", ".5", "+3", "1e5", "abc", "1234567890123456789")
            .forEach { assertNull(WalletDecimal.parse(it), it) }
    }

    @Test
    fun convertsToMinorUnitsOfTheCurrency() {
        assertEquals(Money(-1300, usd), WalletDecimal.parse("-13.00")!!.toMoney(usd))
        assertEquals(Money(150000, usd), WalletDecimal.parse("1500")!!.toMoney(usd))
        assertEquals(Money(1500, kwd), WalletDecimal.parse("1.5")!!.toMoney(kwd))
    }

    @Test
    fun dropsOnlyZeroDigitsBeyondTheCurrency() {
        assertEquals(Money(-500000, jpy), WalletDecimal.parse("-500000.00")!!.toMoney(jpy))
        assertNull(WalletDecimal.parse("10.5")!!.toMoney(jpy))
        assertNull(WalletDecimal.parse("0.001")!!.toMoney(usd))
    }

    @Test
    fun comparesValuesIgnoringTrailingZeros() {
        assertTrue(WalletDecimal.parse("380000.00")!!.sameValueAs(WalletDecimal.parse("380000")!!))
        assertTrue(WalletDecimal.parse("-7600.0")!!.abs().sameValueAs(WalletDecimal.parse("7600.00")!!))
        assertFalse(WalletDecimal.parse("10.01")!!.sameValueAs(WalletDecimal.parse("10.1")!!))
    }
}
