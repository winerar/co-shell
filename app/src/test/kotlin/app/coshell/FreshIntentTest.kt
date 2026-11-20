package app.coshell

import android.content.Intent
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FreshIntentTest {
    @Test
    fun `a new share is taken`() = assertTrue(isFreshIntent(restored = false, flags = Intent.FLAG_ACTIVITY_NEW_TASK))

    @Test
    fun `a restored activity does not take its old intent again`() = assertFalse(isFreshIntent(restored = true, flags = 0))

    @Test
    fun `opening from recents does not repeat the share`() =
        assertFalse(isFreshIntent(restored = false, flags = Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY or Intent.FLAG_ACTIVITY_NEW_TASK))
}
