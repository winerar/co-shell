package app.coshell.core.domain.goals

import app.coshell.core.model.CurrencyCode
import app.coshell.core.model.Goal
import app.coshell.core.model.Money
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.datetime.LocalDate

class GoalBookTest {
    private val usd = CurrencyCode("USD")
    private val ars = CurrencyCode("ARS")
    private fun usd(major: Long) = Money(major * 100, usd)
    private val today = LocalDate(2026, 11, 26)

    private fun goal(id: String, target: Long, account: String = "box", deadline: LocalDate? = null, yearly: Boolean = false) =
        Goal(id, id, usd(target), account, deadline, yearly)

    @Test
    fun `saved is the balance of the goal account`() {
        val lines = GoalBook.of(listOf(goal("trip", 2_000)), mapOf("box" to usd(1_250)), today)
        assertEquals(usd(1_250), lines.single().progress.saved)
    }

    @Test
    fun `goals on one account share it by the nearest deadline`() {
        val goals = listOf(
            goal("bike", 450),
            goal("trip", 2_000, deadline = LocalDate(2027, 7, 1)),
            goal("insurance", 360, deadline = LocalDate(2026, 3, 15), yearly = true),
        )
        val saved = GoalBook.of(goals, mapOf("box" to usd(1_000)), today).associate { it.goal.id to it.progress.saved }
        // Insurance is due first (15 March 2027), then the trip; the bike without a deadline gets what is left.
        assertEquals(usd(360), saved["insurance"])
        assertEquals(usd(640), saved["trip"])
        assertEquals(usd(0), saved["bike"])
    }

    @Test
    fun `reached goals go last and the rest by deadline`() {
        val goals = listOf(goal("a", 100, "x", LocalDate(2027, 5, 1)), goal("b", 100, "y", LocalDate(2027, 1, 1)), goal("c", 50, "z"))
        val lines = GoalBook.of(goals, mapOf("x" to usd(10), "y" to usd(10), "z" to usd(80)), today)
        assertEquals(listOf("b", "a", "c"), lines.map { it.goal.id })
    }

    @Test
    fun `a balance in another currency is converted or counts as nothing`() {
        val goals = listOf(goal("trip", 100, "pesos"))
        val none = GoalBook.of(goals, mapOf("pesos" to Money(100_000_00, ars)), today)
        assertEquals(usd(0), none.single().progress.saved)
        val rated = GoalBook.of(goals, mapOf("pesos" to Money(100_000_00, ars)), today) { m, c -> Money(m.amountMinor / 1_000, c) }
        assertEquals(usd(100), rated.single().progress.saved)
    }

    @Test
    fun `an account in the red saves nothing`() {
        val lines = GoalBook.of(listOf(goal("trip", 100)), mapOf("box" to usd(-50)), today)
        assertEquals(usd(0), lines.single().progress.saved)
    }
}
