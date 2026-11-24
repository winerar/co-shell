package app.coshell.home.sync

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class SyncDotRulesTest {
    @Test
    fun `nothing is shown outside a family`() {
        assertNull(SyncDotRules.of(inFamily = false, pending = 0))
        assertNull(SyncDotRules.of(inFamily = false, pending = 5))
    }

    @Test
    fun `a family with nothing waiting is synced`() {
        assertEquals(SyncDotState.Synced, SyncDotRules.of(inFamily = true, pending = 0))
    }

    @Test
    fun `waiting records are counted`() {
        assertEquals(SyncDotState.Pending(3), SyncDotRules.of(inFamily = true, pending = 3))
    }
}
