package app.coshell.core.domain.quickentry

import app.coshell.core.model.CurrencyCode
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.assertNull

/** Quick notes read by rules (#586): the gold lines of `docs/samples/eval/eval.json` plus the edge cases. */
class QuickNoteParserTest {
    private val ars = CurrencyCode("ARS")

    private class Case(val line: String, val title: String, val daysAgo: Int, val minor: Long)

    private val gold = listOf(
        Case("кофе 3500", "кофе", 0, 350000),
        Case("такси 2000", "такси", 0, 200000),
        Case("хлеб 850 карта", "хлеб", 0, 85000),
        Case("вчера аптека 4200", "аптека", 1, 420000),
        Case("позавчера продукты 18 300 наличные", "продукты", 2, 1830000),
        Case("netflix 6,99 usd", "netflix", 0, 699),
        Case("uber 1850", "uber", 0, 185000),
        Case("cena 12500 ayer", "cena", 1, 1250000),
        Case("farmacia 4.200", "farmacia", 0, 420000),
        Case("café 3 500 tarjeta", "café", 0, 350000),
        Case("lunch 15,50 usd cash", "lunch", 0, 1550),
        Case("бензин 25000 вчера", "бензин", 1, 2500000),
        Case("подарок маме 9 990", "подарок маме", 0, 999000),
        Case("sube 5000", "sube", 0, 500000),
        Case("пицца 7 800 карта вчера", "пицца", 1, 780000),
        Case("1250 kiosco", "kiosco", 0, 125000),
        Case("такси домой 2 300", "такси домой", 0, 230000),
        Case("regalo cumple 14000", "regalo cumple", 0, 1400000),
        Case("кофе и круассан 4 100", "кофе и круассан", 0, 410000),
        Case("coto 52 300 efectivo", "coto", 0, 5230000),
        Case("24000 десерты", "десерты", 0, 2400000),
        Case("37100 аптека", "аптека", 0, 3710000),
        Case("Субе по 30к", "Субе", 0, 3000000),
        Case("овощи 12000", "овощи", 0, 1200000),
        Case("15000 такси", "такси", 0, 1500000),
        Case("подарок 8к", "подарок", 0, 800000),
        Case("коммуналка 45 000", "коммуналка", 0, 4500000),
        Case("18500 продукты вчера", "продукты", 1, 1850000),
        Case("кафе 2300", "кафе", 0, 230000),
        Case("Субе 5к", "Субе", 0, 500000),
        Case("мясо 31200", "мясо", 0, 3120000),
        Case("кофе по 3к", "кофе", 0, 300000),
        Case("40000 бензин", "бензин", 0, 4000000),
        Case("аптека 7к позавчера", "аптека", 2, 700000),
    )

    @Test
    fun `every gold line is read in full by rules`() {
        for (case in gold) {
            val note = QuickNoteParser.parse(case.line, ars)
            assertEquals(case.minor, note?.money?.amountMinor, case.line)
            assertEquals(case.title, note?.title, case.line)
            assertEquals(case.daysAgo, note?.daysAgo, case.line)
        }
    }

    @Test
    fun `thousands with k or к`() {
        assertEquals("Субе по 30000", QuickNoteParser.expandThousands("Субе по 30к"))
        assertEquals("1500 кофе", QuickNoteParser.expandThousands("1,5к кофе"))
        assertEquals("taxi 5000", QuickNoteParser.expandThousands("taxi 5K"))
        assertEquals("кафе 2300", QuickNoteParser.expandThousands("кафе 2300"))
        assertEquals("1 км", QuickNoteParser.expandThousands("1 км"))
    }

    @Test
    fun `account word and currency are reported`() {
        val cash = QuickNoteParser.parse("lunch 15,50 usd cash", ars)!!
        assertEquals(AccountWord.CASH, cash.account)
        assertEquals(CurrencyCode("USD"), cash.money.currency)
        assertEquals(true, cash.currencyFound)
        assertEquals(AccountWord.CARD, QuickNoteParser.parse("café 3 500 tarjeta", ars)!!.account)
        assertNull(QuickNoteParser.parse("кофе 3500", ars)!!.account)
        assertEquals(false, QuickNoteParser.parse("кофе 3500", ars)!!.currencyFound)
    }

    @Test
    fun `a line without an amount is left for the model`() {
        assertNull(QuickNoteParser.parse("купил хлеб", ars))
        assertNull(QuickNoteParser.parse("", ars))
    }

    @Test
    fun `a currency named by a word is read and dropped from the title`() {
        val amd = CurrencyCode("AMD")
        fun read(line: String) = QuickNoteParser.parse(line, amd, setOf(amd, ars))!!
        assertEquals(CurrencyCode("ARS"), read("кофе 3500 песо").money.currency)
        assertEquals("кофе", read("кофе 3500 песо").title)
        assertEquals(CurrencyCode("USD"), read("обед 15 долларов").money.currency)
        assertEquals(CurrencyCode("RUB"), read("метро 70 рублей").money.currency)
        assertEquals(CurrencyCode("RUB"), read("метро 70 руб.").money.currency)
        assertEquals(amd, read("такси 1500 драм").money.currency)
        assertEquals(CurrencyCode("EUR"), read("museo 20 euros").money.currency)
        assertEquals(false, read("кофе 3500").currencyFound)
    }

    @Test
    fun `payment words pick the kind of account`() {
        assertEquals(AccountWord.CASH, QuickNoteParser.parse("такси 2000 нал", ars)!!.account)
        assertEquals(AccountWord.CASH, QuickNoteParser.parse("хлеб 500 налом", ars)!!.account)
        assertEquals(AccountWord.CARD, QuickNoteParser.parse("кофе 3500 с карты", ars)!!.account)
        assertEquals("кофе", QuickNoteParser.parse("кофе 3500 с карты", ars)!!.title)
        assertEquals(AccountWord.CARD, QuickNoteParser.parse("super 12000 débito", ars)!!.account)
    }

    @Test
    fun `slang for cash and card is understood and leaves the title clean`() {
        val cash = listOf("шаурма 4к нал", "такси 2000 кэшем", "рынок 3000 кешем", "хлеб 500 наликом", "сигареты 2500 налички",
            "вода 800 за кэш", "verdulería 5000 en efectivo", "feria 3000 al contado", "taxi 12 cash")
        cash.forEach { line ->
            val note = QuickNoteParser.parse(line, ars)!!
            assertEquals(AccountWord.CASH, note.account, line)
            assertTrue(note.title.none { it.isDigit() } && note.title.split(' ').size == 1, line + " -> " + note.title)
        }
        val card = listOf("аптека 4200 безнал", "обед 9000 по безналу", "кино 7000 карточкой", "super 15000 con tarjeta", "uber 9 card")
        card.forEach { line ->
            val note = QuickNoteParser.parse(line, ars)!!
            assertEquals(AccountWord.CARD, note.account, line)
            assertTrue(note.title.split(' ').size == 1, line + " -> " + note.title)
        }
        assertEquals(null, QuickNoteParser.parse("кофе 3500", ars)!!.account)
    }

    @Test
    fun `words that only say I spent are dropped`() {
        assertEquals("хлеб", QuickNoteParser.parse("купил хлеб 500", ars)!!.title)
        assertEquals("продукты", QuickNoteParser.parse("потратил на продукты 8000", ars)!!.title)
        assertEquals("la luz", QuickNoteParser.parse("pagué la luz 30000", ars)!!.title)
        assertEquals("купил", QuickNoteParser.parse("купил 500", ars)!!.title)
    }
}
