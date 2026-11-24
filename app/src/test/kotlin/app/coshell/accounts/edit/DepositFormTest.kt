package app.coshell.accounts.edit

import app.coshell.core.model.DepositPayout
import app.coshell.core.model.DepositTerms
import kotlinx.datetime.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Test

/** The conditions of a deposit as typed in «Условия вклада» (#634, step 3). */
class DepositFormTest {
    private fun form(rate: String = "8", end: String = "2027-03-15", payout: DepositPayout = DepositPayout.AT_END) =
        AccountEditUiState(depositRate = rate, depositEnd = end, depositPayout = payout)

    @Test
    fun `a rate and a day become terms`() {
        val terms = DepositTerms(800, LocalDate(2027, 3, 15), DepositPayout.AT_END)
        assertEquals(DepositParse.Valid(terms), parseDepositTerms(form()))
        assertEquals(DepositParse.Valid(terms.copy(rateBp = 850, payout = DepositPayout.CAPITALIZED)), parseDepositTerms(form(rate = "8,5", payout = DepositPayout.CAPITALIZED)))
    }

    @Test
    fun `nothing typed is a deposit without conditions`() {
        assertEquals(DepositParse.Valid(null), parseDepositTerms(form(rate = "", end = "")))
    }

    @Test
    fun `one of the two or a wrong number refuses the save`() {
        assertEquals(DepositParse.Invalid, parseDepositTerms(form(end = "")))
        assertEquals(DepositParse.Invalid, parseDepositTerms(form(rate = "")))
        assertEquals(DepositParse.Invalid, parseDepositTerms(form(rate = "0")))
        assertEquals(DepositParse.Invalid, parseDepositTerms(form(rate = "101")))
        assertEquals(DepositParse.Invalid, parseDepositTerms(form(end = "15.03.2027")))
    }

    @Test
    fun `saved terms go back into the fields`() {
        val s = AccountEditUiState().withDepositTerms(DepositTerms(850, LocalDate(2027, 3, 15), DepositPayout.MONTHLY))
        assertEquals("8,5", s.depositRate)
        assertEquals("2027-03-15", s.depositEnd)
        assertEquals(DepositPayout.MONTHLY, s.depositPayout)
    }
}
