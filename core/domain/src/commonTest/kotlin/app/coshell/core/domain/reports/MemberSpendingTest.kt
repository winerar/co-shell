package app.coshell.core.domain.reports

import app.coshell.core.domain.budgets.BudgetFixtures.ars
import app.coshell.core.domain.budgets.BudgetFixtures.categories
import app.coshell.core.domain.budgets.BudgetFixtures.november
import app.coshell.core.domain.budgets.budgetPeriod
import app.coshell.core.model.Money
import app.coshell.core.model.TransactionType
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class MemberSpendingTest {
    private val month = budgetPeriod(november)

    private fun by(author: String?, d: Int, category: String, amount: Money, type: TransactionType = TransactionType.EXPENSE) =
        ReportDay(LocalDate(2026, 11, d), category, type, amount, author)

    private fun spending(vararg days: ReportDay, others: Set<String> = setOf("anya")) =
        spendingByMember(ledger(*days), categories, others, month.start, month.endExclusive)

    @Test
    fun `each member's spending with folders, me — everything not added by the others`() {
        val rows = spending(
            by("misha", 2, "rent", ars(2_620)),
            by(null, 3, "cafe", ars(380)),
            by("anya", 4, "cafe", ars(1_200)),
            by("anya", 5, "home", ars(800)),
            by("anya", 6, "cafe", ars(200), TransactionType.INCOME),
        )

        assertEquals(listOf(null to ars(3_000), "anya" to ars(1_800)), rows.map { it.memberId to it.amount })
        assertEquals(listOf(63, 37), rows.map { it.percent })
        assertEquals(listOf("home" to ars(2_620), "cafe" to ars(380)), rows[0].top)
        assertEquals(listOf("cafe" to ars(1_000), "home" to ars(800)), rows[1].top, "the refund reduces «Кафе»")
    }

    @Test
    fun `a member who spent nothing is left out, at most three folders`() {
        val rows = spending(by(null, 2, "rent", ars(10)), by(null, 2, "cafe", ars(20)), by(null, 3, "utilities", ars(5)), by(null, 4, "gone", ars(1)))

        assertEquals(listOf<String?>(null), rows.map { it.memberId })
        assertEquals(listOf("cafe", "home", "gone"), rows.single().top.map { it.first })
    }

    @Test
    fun `no other members — no card`() {
        assertTrue(spending(by(null, 2, "cafe", ars(10)), others = emptySet()).isEmpty())
    }
}
