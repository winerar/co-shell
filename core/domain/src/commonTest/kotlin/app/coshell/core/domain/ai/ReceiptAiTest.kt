package app.coshell.core.domain.ai

import app.coshell.core.model.CurrencyCode
import app.coshell.core.model.Money
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate

class ReceiptAiTest {
    private val ars = CurrencyCode("ARS")
    private val usd = CurrencyCode("USD")
    private val today = LocalDate(2026, 11, 24)
    private val text = """
        LA ESQUINA SRL
        Fecha 19/11/2026  Ticket 0004-00012345
        Leche 1L        2.150,00
        Pan             1.764,50
        Subtotal       39.914,50
        Descuento      -2.000,00
        TOTAL $        37.914,50
    """.trimIndent()

    private class Fixed(private val answer: String?, override val cheap: Boolean = false) : LanguageModel {
        var asked: String? = null
        override val available = true
        override suspend fun complete(system: String, prompt: String): String? = answer.also { asked = prompt }
    }

    private suspend fun read(answer: String?, body: String = text) = ReceiptAi(Fixed(answer)).read(body, ars, setOf(ars, usd), today)

    @Test
    fun `a total printed in the text gives the receipt with day and shop`() = runTest {
        val receipt = read("""{"total":"37914.50","currency":"ARS","date":"2026-11-19","shop":"La Esquina"}""")!!
        assertEquals(Money(3_791_450, ars), receipt.total)
        assertEquals(LocalDate(2026, 11, 19), receipt.date)
        assertEquals("La Esquina", receipt.merchant)
        assertEquals("", receipt.key)
        assertEquals(ReceiptAi.FORMAT, receipt.format)
    }

    @Test
    fun `a total that is not in the text is never taken`() = runTest {
        assertNull(read("""{"total":"40000","currency":"ARS","date":"2026-11-19","shop":"La Esquina"}"""))
        assertNull(read("""{"total":null}"""))
        assertNull(read("not json"))
        assertNull(read(null))
    }

    @Test
    fun `a doubtful day becomes today, a strange currency the fallback, a shop without letters nothing`() = runTest {
        val future = read("""{"total":"37914.50","currency":"EUR","date":"2027-01-01","shop":"1234"}""")!!
        assertEquals(today, future.date)
        assertEquals(ars, future.total.currency)
        assertEquals("", future.merchant)
        assertEquals(today, read("""{"total":"37914.50","date":"2019-05-01"}""")!!.date)
    }

    @Test
    fun `printed matches the usual ways of writing one sum`() {
        assertTrue(ReceiptAi.printed(3_791_450, 2, "TOTAL 37.914,50"))
        assertTrue(ReceiptAi.printed(3_791_450, 2, "total 37 914,5"))
        assertTrue(ReceiptAi.printed(3_791_400, 2, "Итого 37914"))
        assertTrue(ReceiptAi.printed(699, 2, "Netflix USD 6.99"))
        assertEquals(false, ReceiptAi.printed(3_791_450, 2, "12 37914 50"))
    }

    @Test
    fun `without a model nothing is asked`() = runTest {
        assertNull(ReceiptAi(NoLanguageModel).read(text, ars, setOf(ars), today))
    }

    @Test
    fun `the prompt carries the text and the person's currencies, the cloud flag follows the model`() = runTest {
        val model = Fixed("{}", cheap = true)
        ReceiptAi(model).read(text, ars, setOf(usd), today)
        assertTrue(model.asked!!.contains("TOTAL"))
        assertTrue(model.asked!!.contains("USD | ARS") || model.asked!!.contains("ARS | USD"))
        assertTrue(ReceiptAi(model).cloud)
    }
}
