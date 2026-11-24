package app.coshell.core.domain.debts

import app.coshell.core.domain.FakeAccountRepository
import app.coshell.core.model.Account
import app.coshell.core.model.CurrencyCode
import app.coshell.core.model.DebtDirection
import app.coshell.core.model.DebtEvent
import app.coshell.core.model.DebtEventKind
import app.coshell.core.model.IdGenerator
import app.coshell.core.model.Money
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.time.Instant
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate

class DebtsTest {
    private val usd = CurrencyCode("USD")
    private val ars = CurrencyCode("ARS")
    private val now = Instant.parse("2026-11-23T12:00:00Z")
    private fun money(major: Long, c: CurrencyCode = usd) = Money(major * 100, c)

    private val repo = InMemoryDebtRepository()
    private val accounts = FakeAccountRepository()
    private var next = 0
    private val ids = IdGenerator { "id${next++}" }
    private val issue = IssueDebtUseCase(repo, accounts, ids)
    private val giveBack = ReturnDebtUseCase(repo, accounts, ids)
    private val close = CloseDebtUseCase(repo, ids)

    private suspend fun seed() {
        accounts.saveAccount(Account("cash", "Наличные", usd))
        accounts.saveAccount(Account("pesos", "Pesos", ars))
        accounts.saveAccount(Account("old", "Старый", usd, archived = true))
    }

    @Test
    fun `a loan is issued from an account and shows what is left`() = runTest {
        seed()
        val saved = issue(DebtDirection.LENT, " Саша ", money(300), "cash", now, LocalDate(2026, 12, 5), " за билеты ") as DebtResult.Saved
        val debt = DebtBook.of(repo.events()).single()
        assertEquals(saved.debtId, debt.id)
        assertEquals("Саша", debt.person)
        assertEquals(money(300), debt.left)
        assertEquals("за билеты", debt.note)
        assertFalse(debt.closed)
    }

    @Test
    fun `returns reduce what is left, close it at zero and never exceed it`() = runTest {
        seed()
        val id = (issue(DebtDirection.LENT, "Саша", money(300), "cash", now) as DebtResult.Saved).debtId
        assertEquals(DebtResult.Saved(id), giveBack(id, money(100), "cash", now))
        assertEquals(money(200), DebtBook.of(repo.events()).single().left)
        assertEquals(DebtResult.Invalid(DebtProblem.TOO_MUCH), giveBack(id, money(250), "cash", now))
        giveBack(id, money(200), "cash", now)
        val debt = DebtBook.of(repo.events()).single()
        assertTrue(debt.closed)
        assertEquals(money(300), debt.returned)
        assertEquals(DebtResult.Invalid(DebtProblem.CLOSED), giveBack(id, money(1), "cash", now))
    }

    @Test
    fun `closing forgives the rest without moving any account`() = runTest {
        seed()
        val id = (issue(DebtDirection.BORROWED, "Диа", money(500), "cash", now) as DebtResult.Saved).debtId
        giveBack(id, money(200), "cash", now)
        close(id, now)
        val debt = DebtBook.of(repo.events()).single()
        assertTrue(debt.closed)
        assertEquals(money(300), debt.left)
        assertEquals(DebtResult.Invalid(DebtProblem.CLOSED), close(id, now))
    }

    @Test
    fun `a bad debt is refused with the thing to fix`() = runTest {
        seed()
        assertEquals(DebtResult.Invalid(DebtProblem.PERSON), issue(DebtDirection.LENT, " ", money(1), "cash", now))
        assertEquals(DebtResult.Invalid(DebtProblem.AMOUNT), issue(DebtDirection.LENT, "А", money(0), "cash", now))
        assertEquals(DebtResult.Invalid(DebtProblem.ACCOUNT), issue(DebtDirection.LENT, "А", money(1), "none", now))
        assertEquals(DebtResult.Invalid(DebtProblem.ACCOUNT), issue(DebtDirection.LENT, "А", money(1), "old", now))
        assertEquals(DebtResult.Invalid(DebtProblem.CURRENCY), issue(DebtDirection.LENT, "А", money(1), "pesos", now))
        assertEquals(DebtResult.Invalid(DebtProblem.NOT_FOUND), giveBack("nope", money(1), "cash", now))
        assertTrue(repo.events().isEmpty())
    }

    @Test
    fun `totals add up what is left per currency, active debts of one direction only`() = runTest {
        seed()
        issue(DebtDirection.LENT, "Саша", money(300), "cash", now)
        val closed = (issue(DebtDirection.LENT, "Игорь", money(80), "cash", now) as DebtResult.Saved).debtId
        close(closed, now)
        issue(DebtDirection.LENT, "Лена", Money(3_000_000, ars), "pesos", now)
        issue(DebtDirection.BORROWED, "Диа", money(200), "cash", now)
        val debts = DebtBook.of(repo.events())
        assertEquals(listOf(Money(30_000, usd), Money(3_000_000, ars)).sortedByDescending { it.amountMinor }, DebtBook.totals(debts, DebtDirection.LENT))
        assertEquals(listOf(money(300), Money(3_000_000, ars)).first { it.currency == ars }, DebtBook.totals(debts, DebtDirection.LENT, main = ars).first())
        assertEquals(listOf(money(200)), DebtBook.totals(debts, DebtDirection.BORROWED))
        assertEquals(1, DebtBook.closed(debts, DebtDirection.LENT).size)
    }

    @Test
    fun `reminders list late debts first, then those due within three days and today`() = runTest {
        seed()
        val today = LocalDate(2026, 11, 23)
        issue(DebtDirection.LENT, "Поздно", money(10), "cash", now, today.minusDays(4))
        issue(DebtDirection.LENT, "Скоро", money(20), "cash", now, today.minusDays(-3))
        issue(DebtDirection.LENT, "Сегодня", money(30), "cash", now, today)
        issue(DebtDirection.LENT, "Далеко", money(40), "cash", now, today.minusDays(-10))
        issue(DebtDirection.LENT, "Без срока", money(50), "cash", now)
        val due = DebtBook.due(DebtBook.of(repo.events()), today)
        assertEquals(listOf("Поздно", "Сегодня", "Скоро"), due.map { it.debt.person })
        assertEquals(listOf(DebtBook.Stage.LATE, DebtBook.Stage.TODAY, DebtBook.Stage.SOON), due.map { it.stage })
        assertEquals(4, due.first().days)
    }

    @Test
    fun `an event without its issue is waited for and bad events are refused`() {
        val orphan = DebtEvent("e", "d", DebtEventKind.RETURN, DebtDirection.LENT, money(5), now, "cash")
        assertTrue(DebtBook.of(listOf(orphan)).isEmpty())
        assertFailsWith<IllegalArgumentException> { DebtEvent("e", "d", DebtEventKind.ISSUE, DebtDirection.LENT, money(0), now, "cash", "Саша") }
        assertFailsWith<IllegalArgumentException> { DebtEvent("e", "d", DebtEventKind.ISSUE, DebtDirection.LENT, money(5), now, null, "Саша") }
        assertFailsWith<IllegalArgumentException> { DebtEvent("e", "d", DebtEventKind.CLOSE, DebtDirection.LENT, money(5), now) }
    }

    private fun LocalDate.minusDays(n: Int) = LocalDate.fromEpochDays(toEpochDays() - n)
}
