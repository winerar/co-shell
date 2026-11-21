package app.coshell.core.domain.categoryguess

import app.coshell.core.model.Category
import app.coshell.core.model.CategoryType
import app.coshell.core.model.CurrencyCode
import app.coshell.core.model.Money
import app.coshell.core.model.Transaction
import app.coshell.core.model.TransactionType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.time.Instant

class CategoryGuessTest {
    private val ars = CurrencyCode("ARS")
    private val date = Instant.parse("2026-11-13T21:40:00Z")
    private val categories = listOf(
        Category("food", "Продукты", CategoryType.EXPENSE),
        Category("cafe", "Кафе", CategoryType.EXPENSE),
        Category("old", "Старая", CategoryType.EXPENSE, archived = true),
        Category("salary", "Зарплата", CategoryType.INCOME),
    )
    private var n = 0

    private fun tx(
        category: String?, payee: String? = null, receiptKey: String? = null, note: String = "",
        type: TransactionType = TransactionType.EXPENSE,
    ) = Transaction(
        id = "t${n++}", type = type, accountId = "a", amount = Money.ofMajor(10, currency = ars), date = date,
        categoryId = category, payee = payee, receiptKey = receiptKey, note = note,
    )

    private fun guess(history: List<Transaction>, payee: String? = null, key: String? = null, note: String = "",
        type: TransactionType = TransactionType.EXPENSE) = CategoryGuess.guess(history, type, categories, payee, key, note)

    @Test
    fun `same payee gives its category`() {
        assertEquals("food", guess(listOf(tx("food", payee = "Disco"), tx("cafe", payee = "Bar")), payee = "disco"))
    }

    @Test
    fun `payee noise is ignored for comparison`() {
        assertEquals("food", guess(listOf(tx("food", payee = "MERPAGO*KIOSCO 123")), payee = "  Kiosco  "))
        assertEquals("kiosco", CategoryGuess.normalize("MERPAGO*KIOSCO 123"))
        assertNull(CategoryGuess.normalize("123 *45"))
    }

    @Test
    fun `receipt seller works without a payee`() {
        val history = listOf(tx("cafe", receiptKey = "30685849751:442:6:117817"))
        assertEquals("cafe", guess(history, key = "30685849751:7:6:99"))
        assertNull(guess(history, key = "20111111112:1:6:5"))
    }

    @Test
    fun `note first line matches`() {
        assertEquals("food", guess(listOf(tx("food", note = "Carrefour\nleche — 100")), note = "carrefour"))
    }

    @Test
    fun `payee beats seller and note`() {
        val history = listOf(tx("cafe", note = "Disco"), tx("food", payee = "Disco"))
        assertEquals("food", guess(history, payee = "Disco", note = "Disco"))
    }

    @Test
    fun `most frequent wins and a tie goes to the newer`() {
        val history = listOf(tx("cafe", payee = "X"), tx("food", payee = "X"), tx("food", payee = "X"))
        assertEquals("food", guess(history, payee = "X"))
        assertEquals("cafe", guess(history.take(2), payee = "X"))
    }

    @Test
    fun `archived deleted and wrong type categories are not suggested`() {
        assertNull(guess(listOf(tx("old", payee = "X")), payee = "X"))
        assertNull(guess(listOf(tx("gone", payee = "X")), payee = "X"))
        assertNull(guess(listOf(tx("salary", payee = "X", type = TransactionType.INCOME)), payee = "X"))
        assertEquals("salary", guess(listOf(tx("salary", payee = "X", type = TransactionType.INCOME)), payee = "X", type = TransactionType.INCOME))
    }

    @Test
    fun `new payee and transfers give nothing`() {
        assertNull(guess(listOf(tx("food", payee = "Disco")), payee = "Other"))
        assertNull(guess(listOf(tx("food", payee = "Disco")), payee = null))
        assertNull(guess(listOf(tx("food", payee = "Disco")), payee = "Disco", type = TransactionType.TRANSFER))
    }

    @Test
    fun `an unknown payee falls back to the brand dictionary`() {
        val withIcons = categories + listOf(Category("rest", "Кафе", CategoryType.EXPENSE, icon = "restaurant"))
        val s = CategoryGuess.suggest(emptyList(), TransactionType.EXPENSE, withIcons, payee = "Starbucks")
        assertEquals("rest", s?.categoryId)
        assertEquals(CategoryGuess.Source.DICTIONARY, s?.source)
        // …the note works too, and history beats the dictionary.
        assertEquals("rest", CategoryGuess.suggest(emptyList(), TransactionType.EXPENSE, withIcons, note = "Cafe Martinez\nflat white")?.categoryId)
        val past = listOf(tx("food", payee = "Starbucks"))
        val h = CategoryGuess.suggest(past, TransactionType.EXPENSE, withIcons, payee = "Starbucks")
        assertEquals("food", h?.categoryId)
        assertEquals(CategoryGuess.Source.HISTORY, h?.source)
    }

    @Test
    fun `the dictionary never suggests for income transfers or unknown places`() {
        val withIcons = categories + listOf(Category("rest", "Кафе", CategoryType.EXPENSE, icon = "restaurant"))
        assertNull(CategoryGuess.suggest(emptyList(), TransactionType.INCOME, withIcons, payee = "Starbucks"))
        assertNull(CategoryGuess.suggest(emptyList(), TransactionType.TRANSFER, withIcons, payee = "Starbucks"))
        assertNull(CategoryGuess.suggest(emptyList(), TransactionType.EXPENSE, withIcons, payee = "Juan Pérez"))
        assertNull(CategoryGuess.suggest(emptyList(), TransactionType.EXPENSE, categories, payee = "Starbucks"))
    }
}
