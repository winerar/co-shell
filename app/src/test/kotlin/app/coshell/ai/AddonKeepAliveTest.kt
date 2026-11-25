package app.coshell.ai

import android.content.ServiceConnection
import android.os.Handler
import android.os.Looper
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import java.time.Duration

/** The add-on's service (and the loaded model) outlives one request (#583 «оболочка» п. 7): one binding, let go after a pause. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], application = android.app.Application::class)
class AddonKeepAliveTest {
    private var binds = 0
    private var unbinds = 0
    private var installed = true

    private fun keepAlive() = AddonKeepAlive(
        bind = { _: ServiceConnection -> binds++; installed },
        unbind = { _: ServiceConnection -> unbinds++ },
        handler = Handler(Looper.getMainLooper()),
    )

    private fun pass(ms: Long) = shadowOf(Looper.getMainLooper()).idleFor(Duration.ofMillis(ms))

    @Test
    fun `requests in a row share one binding, released after a quiet pause`() {
        val k = keepAlive()
        k.hold()
        pass(AddonKeepAlive.KEEP_MS - 1_000)
        k.hold()
        pass(AddonKeepAlive.KEEP_MS - 1_000)
        assertTrue(k.held)
        assertEquals(1 to 0, binds to unbinds)
        pass(2_000)
        assertFalse(k.held)
        assertEquals(1 to 1, binds to unbinds)
        k.hold()
        assertEquals(2, binds)
    }

    @Test
    fun `without the add-on nothing is held`() {
        installed = false
        val k = keepAlive()
        k.hold()
        assertFalse(k.held)
        assertEquals(1 to 1, binds to unbinds)
        pass(AddonKeepAlive.KEEP_MS * 2)
        assertEquals(1, unbinds)
    }
}
