package app.coshell.transactions.list

import app.coshell.transactions.list.TransactionsUiState.Day
import app.coshell.transactions.list.TransactionsUiState.Kind
import app.coshell.transactions.list.TransactionsUiState.RelativeDay
import app.coshell.transactions.list.TransactionsUiState.Row
import kotlinx.datetime.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/** The header's «where am I» line names the day at the top of the list (header poll А). */
class DayAtTest {
    private fun row(id: String) = Row(id = id, kind = Kind.EXPENSE, title = "Кафе", subtitle = "Карта", note = "", amount = "-1")
    private val today = Day(LocalDate(2026, 11, 15), RelativeDay.TODAY, listOf(row("a"), row("b")))
    private val yesterday = Day(LocalDate(2026, 11, 14), RelativeDay.YESTERDAY, listOf(row("c")))
    private val state = TransactionsUiState(days = listOf(today, yesterday))

    @Test
    fun `filter bar at the top names no day`() {
        assertNull(dayAt(state, 0))
    }

    @Test
    fun `title and rows of a day name that day`() {
        assertEquals(today, dayAt(state, 1)) // title «Сегодня»
        assertEquals(today, dayAt(state, 3)) // its last row
        assertEquals(yesterday, dayAt(state, 4)) // title «Вчера»
        assertEquals(yesterday, dayAt(state, 5))
    }

    @Test
    fun `past the end stays on the last day, an empty list has none`() {
        assertEquals(yesterday, dayAt(state, 99))
        assertNull(dayAt(TransactionsUiState(), 0))
    }
}
