package app.coshell.core.domain.hints

import app.coshell.core.model.CurrencyCode
import app.coshell.core.model.Money
import app.coshell.core.model.Transaction
import app.coshell.core.model.TransactionType
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlinx.datetime.Instant

class RepeatedSpendingTest {
    private val ars = CurrencyCode("ARS")

    private fun spend(id: String, category: String? = "food", account: String = "card", day: Long = 0) = Transaction(
        id, TransactionType.EXPENSE, account, Money(100, ars), Instant.fromEpochSeconds(day * 86_400), categoryId = category,
    )

    @Test fun `three alike in a row`() =
        assertTrue(RepeatedSpending.hasRun(listOf(spend("a"), spend("b"), spend("c"), spend("d", category = "fun"))))

    @Test fun `fewer than three is not a run`() = assertFalse(RepeatedSpending.hasRun(listOf(spend("a"), spend("b"))))

    @Test fun `another category or account breaks it`() {
        assertFalse(RepeatedSpending.hasRun(listOf(spend("a"), spend("b", category = "fun"), spend("c"))))
        assertFalse(RepeatedSpending.hasRun(listOf(spend("a"), spend("b"), spend("c", account = "cash"))))
    }

    @Test fun `no category or an income is not a run`() {
        assertFalse(RepeatedSpending.hasRun(listOf(spend("a", null), spend("b", null), spend("c", null))))
        val income = Transaction("i", TransactionType.INCOME, "card", Money(100, ars), Instant.fromEpochSeconds(0), categoryId = "food")
        assertFalse(RepeatedSpending.hasRun(listOf(spend("a"), spend("b"), income)))
    }
}
