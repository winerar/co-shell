package app.coshell.core.domain.ai

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest

class RoutedLanguageModelTest {
    private class Fake(override var available: Boolean, var answer: String?) : LanguageModel {
        var cheapValue = false
        override val cheap: Boolean get() = cheapValue
        var calls = 0
        override suspend fun complete(system: String, prompt: String): String? { calls++; return answer }
        var warms = 0
        override suspend fun warm() { warms++ }
    }

    @Test
    fun `only the phone model is warmed and only when the cloud will not answer`() = runTest {
        val cloud = Fake(true, "cloud"); val phone = Fake(true, "phone")
        routed(cloud, phone, on = true).warm()
        assertEquals(0 to 0, cloud.warms to phone.warms)
        routed(cloud, phone, on = false).warm()
        assertEquals(0 to 1, cloud.warms to phone.warms)
        cloud.available = false
        routed(cloud, phone, on = true).warm()
        assertEquals(2, phone.warms)
        phone.available = false
        routed(cloud, phone, on = false).warm()
        assertEquals(2, phone.warms)
    }

    private fun routed(cloud: Fake, phone: Fake, on: Boolean) = RoutedLanguageModel(cloud, phone, object : CloudAiSettings { override val enabled = on })

    @Test
    fun `cloud answers when allowed and reachable`() = runTest {
        val cloud = Fake(true, "cloud"); val phone = Fake(true, "phone")
        assertEquals("cloud", routed(cloud, phone, on = true).complete("s", "p"))
        assertEquals(0, phone.calls)
    }

    @Test
    fun `nothing is sent to the cloud while the switch is off`() = runTest {
        val cloud = Fake(true, "cloud"); val phone = Fake(true, "phone")
        assertEquals("phone", routed(cloud, phone, on = false).complete("s", "p"))
        assertEquals(0, cloud.calls)
    }

    @Test
    fun `phone takes over when the cloud fails`() = runTest {
        val cloud = Fake(true, null); val phone = Fake(true, "phone")
        assertEquals("phone", routed(cloud, phone, on = true).complete("s", "p"))
        assertEquals(1, cloud.calls)
    }

    @Test
    fun `unreachable cloud is not even tried`() = runTest {
        val cloud = Fake(false, "cloud"); val phone = Fake(true, "phone")
        assertEquals("phone", routed(cloud, phone, on = true).complete("s", "p"))
        assertEquals(0, cloud.calls)
    }

    @Test
    fun `no model at all means null and not available`() = runTest {
        val model = routed(Fake(false, "c"), Fake(false, "p"), on = true)
        assertFalse(model.available)
        assertEquals(null, model.complete("s", "p"))
    }

    @Test
    fun `cheap only while the cloud is the one answering`() {
        assertFalse(routed(Fake(true, "c"), Fake(true, "p"), on = false).cheap)
        assertTrue(routed(Fake(true, "c").also { it.cheapValue = true }, Fake(true, "p"), on = true).cheap)
    }

    @Test
    fun `available when only the allowed cloud is there`() {
        assertTrue(routed(Fake(true, "c"), Fake(false, "p"), on = true).available)
        assertFalse(routed(Fake(true, "c"), Fake(false, "p"), on = false).available)
    }
}
