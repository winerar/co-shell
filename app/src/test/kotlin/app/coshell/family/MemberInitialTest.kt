package app.coshell.family

import app.coshell.core.model.Member
import app.coshell.family.components.memberInitial
import org.junit.Assert.assertEquals
import org.junit.Test

class MemberInitialTest {
    @Test
    fun `first letter of the name, upper case`() = assertEquals("М", memberInitial(Member("u", "михаил", "m@x.com")))

    @Test
    fun `no name - first letter of the address`() = assertEquals("A", memberInitial(Member("u", " ", "anna@x.com")))

    @Test
    fun `skips signs before the first letter`() = assertEquals("Б", memberInitial(Member("u", "«Боря»", "")))

    @Test
    fun `nothing to show - a dot`() = assertEquals("·", memberInitial(Member("u", "", "")))
}
