package app.coshell.core.domain.greeting

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ReleaseGreetingTest {
    @Test
    fun `major is read from the version name`() {
        assertEquals(1, ReleaseGreeting.majorOf("1.0"))
        assertEquals(2, ReleaseGreeting.majorOf("2.15"))
        assertEquals(1, ReleaseGreeting.majorOf("1.4-debug"))
        assertEquals(0, ReleaseGreeting.majorOf("0.37"))
        assertNull(ReleaseGreeting.majorOf("beta"))
    }

    @Test
    fun `due on 1_0 and newer for a member who is not the owner`() {
        assertTrue(ReleaseGreeting.isDue("1.0", isMember = true, isOwner = false))
        assertTrue(ReleaseGreeting.isDue("1.7", isMember = true, isOwner = false))
        assertTrue(ReleaseGreeting.isDue("2.0", isMember = true, isOwner = false))
    }

    @Test
    fun `not due before 1_0`() {
        assertFalse(ReleaseGreeting.isDue("0.99", isMember = true, isOwner = false))
        assertFalse(ReleaseGreeting.isDue("0.0-debug", isMember = true, isOwner = false))
        assertFalse(ReleaseGreeting.isDue("beta", isMember = true, isOwner = false))
    }

    @Test
    fun `not due for the owner or without a family`() {
        assertFalse(ReleaseGreeting.isDue("1.0", isMember = true, isOwner = true))
        assertFalse(ReleaseGreeting.isDue("1.0", isMember = false, isOwner = false))
    }
}
