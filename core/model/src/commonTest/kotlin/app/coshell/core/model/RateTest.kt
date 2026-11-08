package app.coshell.core.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

class RateTest {
    private val usd = CurrencyCode("USD")
    private val ars = CurrencyCode("ARS")
    private val amd = CurrencyCode("AMD")
    private val jpy = CurrencyCode("JPY")
    private val btc = CurrencyCode("BTC")
    private val kwd = CurrencyCode("KWD")

    @Test
    fun `parses and prints plain decimals`() {
        assertEquals("1234.5", Rate.parse("1234.50").toPlainString())
        assertEquals("0.00012", Rate.parse("0,00012").toPlainString())
        assertEquals("1000", Rate.parse("1000").toPlainString())
        assertEquals("1", Rate.parse("001.000").toPlainString())
        assertEquals(Rate.parse("2.5"), Rate.parse("2.50000"))
    }

    @Test
    fun `rejects non positive or malformed text`() {
        listOf("", "0", "0.000", "-1", "1e5", "abc", "1.2.3", ".5", "1.").forEach {
            assertNull(Rate.parseOrNull(it), "'$it' must be rejected")
        }
        assertNull(Rate.parseOrNull("0." + "0".repeat(60) + "1"))
        assertFailsWith<IllegalArgumentException> { Rate.parse("0") }
    }

    @Test
    fun `rounds extra precision half to even`() {
        assertEquals("1.23456789012346", Rate.parse("1.234567890123456").toPlainString())
        assertEquals("1", Rate.parse("1.000000000000005").toPlainString())
        assertEquals("1.00000000000002", Rate.parse("1.000000000000015").toPlainString())
        assertEquals("0.000000000000000001", Rate.parse("0.0000000000000000012").toPlainString())
    }

    @Test
    fun `inverse and chained rates`() {
        assertEquals("0.001", Rate.parse("1000").inverse().toPlainString())
        assertEquals("0.333333333333333", Rate.parse("3").inverse().toPlainString())
        assertEquals("1.5", Rate.parse("2").inverse().times(Rate.parse("3")).toPlainString())
        assertEquals("2.5", Rate.parse("0.4").inverse().toPlainString())
        assertEquals(Rate.ONE, Rate.parse("4").times(Rate.parse("0.25")))
    }

    @Test
    fun `converts money with fraction digits of both currencies`() {
        // 100.00 USD at 1050.25 ARS/USD = 105 025.00 ARS
        assertEquals(Money(10_502_500, ars), Rate.parse("1050.25").convert(Money(10_000, usd), ars))
        // 1 000 JPY (no minor units) at 0.0067 USD/JPY = 6.70 USD
        assertEquals(Money(670, usd), Rate.parse("0.0067").convert(Money(1_000, jpy), usd))
        // 0.01 USD at 150 JPY/USD = 1.5 JPY → 2 (half to even)
        assertEquals(Money(2, jpy), Rate.parse("150").convert(Money(1, usd), jpy))
        // 0.03 USD at 150 = 4.5 JPY → 4 (half to even)
        assertEquals(Money(4, jpy), Rate.parse("150").convert(Money(3, usd), jpy))
        // 1.000 KWD at 3.25 USD/KWD = 3.25 USD
        assertEquals(Money(325, usd), Rate.parse("3.25").convert(Money(1_000, kwd), usd))
        // 50 000.00 USD at 0.00001 BTC/USD = 0.5 BTC (8 digits)
        assertEquals(Money(50_000_000, btc), Rate.parse("0.00001").convert(Money(5_000_000, usd), btc))
    }

    @Test
    fun `keeps the sign and handles big amounts without overflow`() {
        assertEquals(Money(-38_700, amd), Rate.parse("387").convert(Money(-100, usd), amd))
        // 10^15 minor units × a 15-digit mantissa needs 128-bit intermediate math.
        val huge = Money(1_000_000_000_000_000, amd)
        assertEquals(Money(2_583_979_328_165, usd), Rate.parse("0.002583979328165").convert(huge, usd))
        assertFailsWith<ArithmeticException> {
            Rate.parse("1000000").convert(Money(Long.MAX_VALUE / 2, usd), ars)
        }
    }

    @Test
    fun `real rate of a deal from two amounts`() {
        val usdt = CurrencyCode("USDT")
        // 14.29 USDT debited for a 15 000 ARS receipt
        val rate = Rate.between(Money(1_429, usdt), Money(1_500_000, ars))
        assertEquals("1049.68509447166", rate.toPlainString())
        assertEquals("1049.69", rate.rounded(2).toPlainString())
        assertEquals("0.000953", rate.inverse().rounded(6).toPlainString())
        // Different fraction digits: 1 000 JPY for 6.70 USD → 1 JPY = 0.0067 USD
        assertEquals(Rate.parse("0.0067"), Rate.between(Money(1_000, jpy), Money(670, usd)))
        assertEquals(Rate.parse("150"), Rate.between(Money(100, usd), Money(150, jpy)).rounded(10))
        // Extreme magnitudes stay within 128 bits.
        assertEquals("0.000000000000001", Rate.between(Money(1_000_000_000_000_000, amd), Money(1, usd)).toPlainString())
        assertEquals("100000000000000000", Rate.between(Money(1, usd), Money(100_000_000_000_000_000, usd)).toPlainString())
    }

    @Test
    fun `compares by value`() {
        kotlin.test.assertTrue(Rate.parse("2.5") > Rate.parse("2.49999"))
        kotlin.test.assertTrue(Rate.parse("0.001") < Rate.ONE)
        assertEquals(0, Rate.parse("100").compareTo(Rate.parse("100.000")))
    }

    @Test
    fun `rounded keeps rates that already fit`() {
        assertEquals(Rate.parse("2.5"), Rate.parse("2.5").rounded(2))
        assertEquals(Rate.parse("2.46"), Rate.parse("2.455").rounded(2))
        assertEquals(Rate.parse("2"), Rate.parse("2.5").rounded(0)) // half to even
    }
}
