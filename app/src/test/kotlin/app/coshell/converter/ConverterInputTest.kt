package app.coshell.converter

import app.coshell.core.designsystem.components.keypad.KeypadKey
import app.coshell.core.domain.settings.ConverterPick
import app.coshell.core.model.CurrencyCode
import app.coshell.core.model.Money
import org.junit.Assert.assertEquals
import org.junit.Test

class ConverterInputTest {
    private val usd = CurrencyCode("USD")
    private val ars = CurrencyCode("ARS")
    private val jpy = CurrencyCode("JPY")
    private val amd = CurrencyCode("AMD")

    private fun input(text: String = "", active: ConverterSide = ConverterSide.FROM) = ConverterInput(usd, ars, text, active)

    @Test
    fun `typing starts from nothing and follows the active currency`() {
        val typed = input().type(KeypadKey.Digit('1')).type(KeypadKey.Digit('2')).type(KeypadKey.Decimal)
        assertEquals("12,", typed.text)
        assertEquals("12", ConverterInput(jpy, usd, "12").type(KeypadKey.Decimal).text)
    }

    @Test
    fun `a tap on the other side starts typing afresh there`() {
        val moved = input("100").activate(ConverterSide.TO)
        assertEquals(ConverterSide.TO, moved.active)
        assertEquals("", moved.text)
        assertEquals(ars, moved.source)
        assertEquals(input("100"), input("100").activate(ConverterSide.FROM))
    }

    @Test
    fun `swap exchanges the currencies and keeps the number`() {
        val swapped = input("100").swap()
        assertEquals(ars, swapped.from)
        assertEquals(usd, swapped.to)
        assertEquals("100", swapped.text)
    }

    @Test
    fun `picking the currency of the other side swaps instead of doubling it`() {
        val picked = input("5").pick(ConverterSide.FROM, ars)
        assertEquals(ars, picked.from)
        assertEquals(usd, picked.to)
    }

    @Test
    fun `picking a currency without a fraction drops the fraction of the typed number`() {
        val picked = input("12,5 + 3,25").pick(ConverterSide.FROM, jpy)
        assertEquals(jpy, picked.from)
        assertEquals("12 + 3", picked.text)
        assertEquals("12,5", input("12,5").pick(ConverterSide.TO, jpy).text) // the other side: the number is not touched
    }

    @Test
    fun `a row of the list becomes the source and the old source takes its place`() {
        val third = input("7").source(amd)
        assertEquals(amd, third.from)
        assertEquals(ars, third.to)
        val second = input("7").source(ars)
        assertEquals(ars, second.from)
        assertEquals(usd, second.to)
        assertEquals(input("7"), input("7").source(usd))
    }

    @Test
    fun `the list needs the number on from - with it on the second side the pair swaps and nothing is lost`() {
        val forAll = input("40", ConverterSide.TO).forAll()
        assertEquals(ars, forAll.from)
        assertEquals(usd, forAll.to)
        assertEquals("40", forAll.text)
        assertEquals(ConverterSide.FROM, forAll.active)
        assertEquals(ars, forAll.source)
    }

    @Test
    fun `keypad sum adds terms and reads an unfinished fraction`() {
        assertEquals(Money.ofMajor(1_512, currency = usd), keypadMoney("1500 + 12,", usd))
        assertEquals(Money.ofMajor(12, 50, currency = usd), keypadMoney("12,5", usd))
        assertEquals(Money.zero(usd), keypadMoney("", usd))
    }

    @Test
    fun `the pair opens with the remembered currencies, else the main one and the first other with a rate`() {
        val options = listOf(usd, amd, ars)
        val rated = setOf(ars)
        assertEquals(usd to ars, resolvePair(options, usd, ConverterPick(), rated))
        assertEquals(amd to ars, resolvePair(options, usd, ConverterPick(from = amd, to = ars), rated))
        assertEquals(usd to amd, resolvePair(options, usd, ConverterPick(), emptySet()))
        // a remembered currency that left the list, or a pair that doubled one currency, falls back
        assertEquals(usd to ars, resolvePair(options, usd, ConverterPick(from = CurrencyCode("EUR"), to = CurrencyCode("EUR")), rated))
        assertEquals(ars to usd, resolvePair(listOf(ars, usd), ars, ConverterPick(from = ars, to = ars), setOf(usd)))
        assertEquals(usd to usd, resolvePair(listOf(usd), usd, ConverterPick(), emptySet()))
    }
}
