package app.coshell.access

import org.junit.Test
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull

class InviteRowTest {
    @Test fun `granted code shows who it was issued to`() =
        assertEquals(InviteRow("Alex", revoked = false), AccessState.Granted("Alex").inviteRow())

    @Test fun `revoked code is marked`() =
        assertEquals(InviteRow("Alex", revoked = true), AccessState.Revoked("Alex").inviteRow())

    @Test fun `no row without a gate or a code`() {
        assertNull(AccessState.Open.inviteRow())
        assertNull(AccessState.Loading.inviteRow())
        assertNull(AccessState.Required.inviteRow())
    }
}
